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
            getTargets(project).each { String name, Target target ->
                target.resolve()
            }
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

    private static List<BuckRule> createRules(Project project) {
        def rules = []
        getTargets(project).each { String name, Target target ->
            switch (ProjectUtil.getType(project)) {
                case ProjectType.JAVA_LIB:
                    rules << JavaLibraryRuleComposer.compose(target as JavaLibTarget)
                    break
                case ProjectType.JAVA_APP:
                    rules << JavaLibraryRuleComposer.compose(target as JavaLibTarget)
                    rules << JavaBinaryRuleComposer.compose(target as JavaAppTarget)
                    break
                case ProjectType.ANDROID_LIB:
                    rules << AndroidLibraryRuleComposer.compose(target as AndroidLibTarget)
                    break
                case ProjectType.ANDROID_APP:
                    rules += createRules((AndroidAppTarget) target)
                    break
                default:
                    break
            }
        }
        return rules
    }

    private static List<BuckRule> createRules(AndroidAppTarget target) {
        def rules = []
        rules << AndroidLibraryRuleComposer.compose(target as AndroidLibTarget)
        rules << AndroidBinaryRuleComposer.compose(target, [":${AndroidBuckRuleComposer.src(target)}"])
        return rules
    }
}
