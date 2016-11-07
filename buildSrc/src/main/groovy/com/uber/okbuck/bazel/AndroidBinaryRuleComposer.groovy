package com.uber.okbuck.bazel

import com.uber.okbuck.composer.AndroidBuckRuleComposer
import com.uber.okbuck.core.model.AndroidAppTarget

final class AndroidBinaryRuleComposer extends AndroidBuckRuleComposer {
    static AndroidBinaryRule compose(
            AndroidAppTarget target,
            List<String> deps) {
        return new AndroidBinaryRule(
                bin(target),
                target.getPackage(),
                ["//visibility:public"],
                deps,
                target.manifest,
                target.proguardConfig,
                target.placeholders)
    }
}