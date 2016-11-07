package com.uber.okbuck.bazel

import com.uber.okbuck.config.BUCKFile
import com.uber.okbuck.composer.AndroidBuckRuleComposer
import com.uber.okbuck.core.model.AndroidAppTarget
import com.uber.okbuck.core.model.AndroidLibTarget
import com.uber.okbuck.core.model.JavaAppTarget
import com.uber.okbuck.core.model.JavaLibTarget
import com.uber.okbuck.core.model.ProjectType
import com.uber.okbuck.core.model.Target
import com.uber.okbuck.core.util.ProjectUtil
import com.uber.okbuck.rule.BuckRule
import org.gradle.api.Project

import static com.uber.okbuck.core.util.ProjectUtil.getTargets

final class BuildFileGenerator {

    static Map<Project, BUCKFile> generate(Project rootProject) {
        rootProject.okbuck.buckProjects.each { Project project ->
            resolve(project)
        }

        Map<Project, List<BuckRule>> projectRules =
                rootProject.okbuck.buckProjects.collectEntries { Project project ->
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
        getTargets(project).each { String name, Target target ->
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
        return rules
    }

    private static List<BuckRule> createRules(JavaAppTarget target) {
        List<BuckRule> rules = []
        rules.addAll(createRules((JavaLibTarget) target))
        rules.add(JavaBinaryRuleComposer.compose(target))
        return rules
    }

    private static List<BuckRule> createRules(AndroidLibTarget target) {
        return [AndroidLibraryRuleComposer.compose(target)]
    }

    private static List<BuckRule> createRules(AndroidAppTarget target) {
        List<BuckRule> rules = []
        List<String> deps = [":${AndroidBuckRuleComposer.src(target)}"]

        Set<BuckRule> libRules = createRules((AndroidLibTarget) target)
        rules.addAll(libRules)
        rules.add(AndroidBinaryRuleComposer.compose(target, deps))
        return rules
    }
}