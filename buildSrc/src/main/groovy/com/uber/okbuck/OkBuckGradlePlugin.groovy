package com.uber.okbuck

import com.uber.okbuck.config.BUCKFile
import com.uber.okbuck.core.dependency.DependencyCache
import com.uber.okbuck.core.task.OkBuckCleanTask
import com.uber.okbuck.core.util.RobolectricUtil
import com.uber.okbuck.extension.ExperimentalExtension
import com.uber.okbuck.extension.GradleGenExtension
import com.uber.okbuck.extension.IntellijExtension
import com.uber.okbuck.extension.OkBuckExtension
import com.uber.okbuck.extension.TestExtension
import com.uber.okbuck.generator.BuckFileGenerator
import com.uber.okbuck.generator.DotBuckConfigLocalGenerator
import com.uber.okbuck.wrapper.BuckWrapperTask
import com.uber.okbuck.extension.WrapperExtension
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger

class OkBuckGradlePlugin implements Plugin<Project> {

    static final String OKBUCK = "okbuck"
    static final String OKBUCK_CLEAN = 'okbuckClean'
    static final String BUCK = "BUCK"
    static final String EXPERIMENTAL = "experimental"
    static final String INTELLIJ = "intellij"
    static final String TEST = "test"
    static final String GRADLE_GEN = "gradleGen"
    static final String WRAPPER = "wrapper"
    static final String BUCK_WRAPPER = "buckWrapper"
    static final String DEFAULT_CACHE_PATH = ".okbuck/cache"
    static final String CONFIGURATION_POST_PROCESS = "postProcess"

    static final String GROUP = "okbuck"

    static DependencyCache depCache
    static Logger LOGGER

    void apply(Project project) {
        LOGGER = project.logger
        OkBuckExtension okbuck = project.extensions.create(OKBUCK, OkBuckExtension, project)
        WrapperExtension wrapper = okbuck.extensions.create(WRAPPER, WrapperExtension)
        ExperimentalExtension experimental = okbuck.extensions.create(EXPERIMENTAL, ExperimentalExtension)
        TestExtension test = okbuck.extensions.create(TEST, TestExtension)
        IntellijExtension intellij = okbuck.extensions.create(INTELLIJ, IntellijExtension)
        okbuck.extensions.create(GRADLE_GEN, GradleGenExtension, project)

        Task okBuck = project.task(OKBUCK)
        okBuck.setGroup(GROUP)
        okBuck.setDescription("Generate BUCK files")
        okBuck.outputs.upToDateWhen { false }

        project.configurations.maybeCreate(CONFIGURATION_POST_PROCESS)

        project.afterEvaluate {
            Task okBuckClean = project.tasks.create(OKBUCK_CLEAN, OkBuckCleanTask, {
                dir = project.projectDir.absolutePath
                includes = wrapper.remove
                excludes = wrapper.keep
            })
            okBuckClean.setGroup(GROUP)
            okBuckClean.setDescription("Delete configuration files generated by OkBuck")

            okBuck.dependsOn(okBuckClean)
            okBuck << {
                generate(project)
            }

            depCache = new DependencyCache(project, DEFAULT_CACHE_PATH, true, true, intellij.sources)

            if (test.robolectric) {
                Task fetchRobolectricRuntimeDeps = project.task('fetchRobolectricRuntimeDeps')
                okBuck.dependsOn(fetchRobolectricRuntimeDeps)
                fetchRobolectricRuntimeDeps.mustRunAfter(okBuckClean)
                fetchRobolectricRuntimeDeps.setDescription("Fetches runtime dependencies for robolectric")

                fetchRobolectricRuntimeDeps << {
                    RobolectricUtil.download(project)
                }
            }

            BuckWrapperTask buckWrapper = project.tasks.create(BUCK_WRAPPER, BuckWrapperTask, {
                repo = wrapper.repo
                remove = wrapper.remove
                keep = wrapper.keep
                watch = wrapper.watch
                sourceRoots = wrapper.sourceRoots
            })
            buckWrapper.setGroup(GROUP)
            buckWrapper.setDescription("Create buck wrapper")
        }
    }

    private static generate(Project project) {
        OkBuckExtension okbuck = project.okbuck

        // generate empty .buckconfig if it does not exist
        File dotBuckConfig = project.file(".buckconfig")
        if (!dotBuckConfig.exists()) {
            dotBuckConfig.createNewFile()
        }

        // generate .buckconfig.local
        File dotBuckConfigLocal = project.file(".buckconfig.local")
        PrintStream configPrinter = new PrintStream(dotBuckConfigLocal)
        DotBuckConfigLocalGenerator.generate(okbuck).print(configPrinter)
        IOUtils.closeQuietly(configPrinter)

        // generate BUCK file for each project
        Map<Project, BUCKFile> buckFiles = BuckFileGenerator.generate(project)

        buckFiles.each { Project subProject, BUCKFile buckFile ->
            PrintStream buckPrinter = new PrintStream(subProject.file(BUCK))
            buckFile.print(buckPrinter)
            IOUtils.closeQuietly(buckPrinter)
        }
    }
}
