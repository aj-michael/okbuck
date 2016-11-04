package com.uber.okbuck.bazel

import com.uber.okbuck.OkBuckGradlePlugin
import com.uber.okbuck.config.BUCKFile
import com.uber.okbuck.core.util.FileUtil
import com.uber.okbuck.extension.OkBuckExtension
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class OkBazelGradlePlugin implements Plugin<Project> {
    static final String DEFAULT_CACHE_PATH = "okbuck/cache"

    @Override
    void apply(Project project) {
        project.extensions.create("okbuck", OkBuckExtension, project)

        Task okBazel = project.task("okbazel")
        okBazel.setGroup("okbazel")
        okBazel.setDescription("Generate BUILD files")
        okBazel.outputs.upToDateWhen { false }

        project.afterEvaluate {
            okBazel << {
                File workspaceFile = project.file("WORKSPACE")
                if (!workspaceFile.exists()) {
                    FileUtil.copyResourceToProject("bazel/WORKSPACE", workspaceFile)
                }

                BuildFileGenerator.generate(project).each { Project subProject, BUCKFile buildFile ->
                    PrintStream printer = new NoTabsPrintStream(subProject.file("BUILD"))
                    buildFile.print(printer)
                    IOUtils.closeQuietly(printer)
                }
            }
            // Yes, this is an awful hack. But I'm trying not to refactor existing OkBuck code.
            OkBuckGradlePlugin.depCache = new BazelDependencyCache(project, DEFAULT_CACHE_PATH)
        }
    }
}