package com.uber.okbuck.bazel

import com.uber.okbuck.composer.JavaBuckRuleComposer
import com.uber.okbuck.core.model.JavaLibTarget

final class JavaLibraryRuleComposer extends JavaBuckRuleComposer {

    private JavaLibraryRuleComposer() {
        // no instance
    }

    static JavaLibraryRule compose(JavaLibTarget target) {
        List<String> deps = []
        deps.addAll(external(target.main.externalDeps))
        deps.addAll(targets(target.main.targetDeps))

        new JavaLibraryRule(
                src(target),
                ["//visibility:public"],
                deps,
                target.main.sources,
                target.main.resourcesDir,
                target.main.jvmArgs)
    }

}