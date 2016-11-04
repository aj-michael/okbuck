package com.uber.okbuck.bazel

import com.uber.okbuck.composer.AndroidBuckRuleComposer
import com.uber.okbuck.core.model.AndroidLibTarget
import com.uber.okbuck.core.model.AndroidTarget
import com.uber.okbuck.core.model.Target

final class AndroidLibraryRuleComposer extends AndroidBuckRuleComposer {

    static AndroidLibraryRule compose(
            AndroidLibTarget target,
            List<String> deps) {

        List<String> libraryDeps = new ArrayList<>(deps);
        Set<String> providedDeps = []

        libraryDeps.addAll(external(target.main.externalDeps))
        libraryDeps.addAll(targets(target.main.targetDeps))

        providedDeps.addAll(external(target.apt.externalDeps))
        providedDeps.addAll(targets(target.apt.targetDeps))
        providedDeps.removeAll(libraryDeps)

        target.main.targetDeps.each { Target targetDep ->
            if (targetDep instanceof AndroidTarget) {
                targetDep.resources.each { AndroidTarget.ResBundle bundle ->
                    libraryDeps.add(res(targetDep as AndroidTarget, bundle))
                }
            }
        }

        return new AndroidLibraryRule(
                src(target),
                ["//visibility:public"],
                libraryDeps,
                target.main.sources,
                target.manifest,
                providedDeps,
                "")
    }
}
