package com.uber.okbuck.bazel

import com.uber.okbuck.composer.JavaBuckRuleComposer
import com.uber.okbuck.core.model.JavaLibTarget

final class JavaLibraryRuleComposer extends JavaBuckRuleComposer {
    static JavaLibraryRule compose(JavaLibTarget target) {
        List<String> deps = []
        deps.addAll(BazelDependencyCache.external(target.main.externalDeps))
        deps.addAll(targets(target.main.targetDeps))
        new JavaLibraryRule(
                "java_library",
                src(target),
                ["//visibility:public"],
                deps,
                target.main.sources,
                target.main.resourcesDir)
    }
}
