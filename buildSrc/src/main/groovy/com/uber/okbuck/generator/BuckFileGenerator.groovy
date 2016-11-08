package com.uber.okbuck.generator

import com.uber.okbuck.composer.*
import com.uber.okbuck.config.BUCKFile
import com.uber.okbuck.core.model.*
import com.uber.okbuck.core.util.ProjectUtil
import com.uber.okbuck.extension.OkBuckExtension
import com.uber.okbuck.rule.AndroidLibraryRule
import com.uber.okbuck.rule.AndroidResourceRule
import com.uber.okbuck.rule.BuckRule
import org.gradle.api.Project

import static com.uber.okbuck.core.util.ProjectUtil.getTargets

final class BuckFileGenerator {

    private BuckFileGenerator(){ }

    /**
     * generate {@code BUCKFile}
     */
    static Map<Project, BUCKFile> generate(Project rootProject) {
        OkBuckExtension okbuck = rootProject.okbuck
        okbuck.buckProjects.each { Project project ->
            resolve(project)
        }

        //TestExtension test = okbuck.test
        Map<Project, List<BuckRule>> projectRules = okbuck.buckProjects.collectEntries { Project project ->
            List<BuckRule> rules = createRules(project)
            [project, rules]
        }

        return projectRules.findAll { Project project, List<BuckRule> rules ->
            !rules.empty
        }.collectEntries { Project project, List<BuckRule> rules ->
            [project, new BUCKFile(rules)]
        } as Map<Project, BUCKFile>
    }

    private static void resolve(Project project) {
        Map<String, Target> targets = getTargets(project)

        targets.each { String name, Target target ->
            target.resolve()
        }
    }

    private static List<BuckRule> createRules(Project project) {
        List<BuckRule> rules = []
        ProjectType projectType = ProjectUtil.getType(project)
        getTargets(project).each { String name, Target target ->
            switch (projectType) {
                case ProjectType.JAVA_LIB:
                    rules.addAll(createRules((JavaLibTarget) target))
                    break
                case ProjectType.JAVA_APP:
                    rules.addAll(createRules((JavaAppTarget) target))
                    break
                case ProjectType.ANDROID_LIB:
                    rules.addAll(createRules((AndroidLibTarget) target))
                    break
                case ProjectType.ANDROID_APP:
                    List<BuckRule> targetRules = createRules((AndroidAppTarget) target)
                    rules.addAll(targetRules)
                    break
                default:
                    break
            }
        }

        // de-dup rules by name
        rules = rules.unique { rule ->
            rule.name
        }

        return rules
    }

    private static List<BuckRule> createRules(JavaLibTarget target) {
        List<BuckRule> rules = []
        rules.add(JavaLibraryRuleComposer.compose(
                target))

        if (target.test.sources) {
            rules.add(JavaTestRuleComposer.compose(
                    target))
        }
        return rules
    }

    private static List<BuckRule> createRules(JavaAppTarget target) {
        List<BuckRule> rules = []
        rules.addAll(createRules((JavaLibTarget) target))
        rules.add(JavaBinaryRuleComposer.compose(target))
        return rules
    }

    /*private static List<BuckRule> createRules(AndroidLibTarget target, String appClass = null, List<String> extraDeps = []) {
        List<BuckRule> rules = []
        List<BuckRule> androidLibRules = []

        // Aidl
        List<BuckRule> aidlRules = target.aidl.collect { String aidlDir ->
            GenAidlRuleComposer.compose(target, aidlDir)
        }
        List<String> aidlRuleNames = aidlRules.collect { GenAidlRule rule ->
            ":${rule.name}"
        }
        androidLibRules.addAll(aidlRules)


        // Res
        androidLibRules.addAll(target.resources.collect { AndroidTarget.ResBundle resBundle ->
            AndroidResourceRuleComposer.compose(target, resBundle)
        })

        // BuildConfig
        //androidLibRules.add(AndroidBuildConfigRuleComposer.compose(target))

        // Apt
        List<String> aptDeps = []
        if (!target.annotationProcessors.empty && !target.apt.externalDeps.empty) {
            AptRule aptRule = AptRuleComposer.compose(target)
            rules.add(aptRule)
            aptDeps.add(":${aptRule.name}")
        }
        aptDeps.addAll(BuckRuleComposer.targets(target.apt.targetDeps))

        // Jni
        androidLibRules.addAll(target.jniLibs.collect { String jniLib ->
            PreBuiltNativeLibraryRuleComposer.compose(target, jniLib)
        });

        List<String> deps = androidLibRules.collect { BuckRule rule ->
            ":${rule.name}"
        } as List<String>
        deps.addAll(extraDeps)

        // Gradle generate sources tasks
        OkBuckExtension okbuck = target.rootProject.okbuck
        GradleGenExtension gradleGen = okbuck.gradleGen
        List<GradleSourceGenRule> sourcegenRules = GradleSourceGenRuleComposer.compose(target, gradleGen)
        List<ZipRule> zipRules = sourcegenRules.collect { GradleSourceGenRule sourcegenRule ->
            ZipRuleComposer.compose(sourcegenRule)
        }
        rules.addAll(sourcegenRules)
        rules.addAll(zipRules)
        Set<String> zipRuleNames = zipRules.collect { ZipRule rule ->
            ":${rule.name}"
        }

        // Lib
        androidLibRules.add(AndroidLibraryRuleComposer.compose(
                target,
                deps,
                aptDeps,
                new ArrayList<String>(),
                appClass,
                zipRuleNames
        ))

        // Test
        if (target.robolectric && target.test.sources) {
            androidLibRules.add(AndroidTestRuleComposer.compose(
                    target,
                    deps,
                    aptDeps,
                    aidlRuleNames,
                    appClass))
        }

        rules.addAll(androidLibRules)
        return rules
    }*/

    private static List<BuckRule> createRules(AndroidAppTarget target) {
        List<BuckRule> rules = []
        List<String> deps = [":${AndroidBuckRuleComposer.src(target)}"]

        /*Set<BuckRule> libRules = createRules((AndroidLibTarget) target,
                target.exopackage ? target.exopackage.appClass : null)
        rules.addAll(libRules)

        libRules.each { BuckRule rule ->
            if (rule instanceof AndroidResourceRule && rule.name != null) {
                deps.add(":${rule.name}")
            }
        }

        AndroidManifestRule manifestRule = AndroidManifestRuleComposer.compose(target)
        rules.add(manifestRule)
        */

        //String keystoreRuleName = KeystoreRuleComposer.compose(target)

        /*if (target.exopackage) {
            ExopackageAndroidLibraryRule exoPackageRule =
                    ExopackageAndroidLibraryRuleComposer.compose(
                            target)
            rules.add(exoPackageRule)
            deps.add(":${exoPackageRule.name}")
        }*/

        rules.add(AndroidBinaryRuleComposer.compose(target, deps))

        return rules
    }

    /*private static List<BuckRule> createRules(AndroidInstrumentationTarget target, AndroidAppTarget mainApkTarget,
                                      List<BuckRule> mainApkTargetRules) {
        List<BuckRule> rules = []

        Set<BuckRule> libRules = createRules((AndroidLibTarget) target, null, filterAndroidDepRules(mainApkTargetRules))
        rules.addAll(libRules)

        AndroidManifestRule manifestRule = AndroidManifestRuleComposer.compose(target, target.instrumentation)
        rules.add(manifestRule)

        rules.add(AndroidInstrumentationApkRuleComposer.compose(filterAndroidDepRules(rules), ":${manifestRule.name}", mainApkTarget))
        rules.add(AndroidInstrumentationTestRuleComposer.compose(mainApkTarget))
        return rules
    }*/

    private static List<String> filterAndroidDepRules(List<BuckRule> rules) {
        return rules.findAll { BuckRule rule ->
            rule instanceof AndroidLibraryRule || rule instanceof AndroidResourceRule
        }.collect {
            ":${it.name}"
        }
    }
}
