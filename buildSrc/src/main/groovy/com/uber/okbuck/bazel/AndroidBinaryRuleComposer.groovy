package com.uber.okbuck.bazel

import com.uber.okbuck.composer.AndroidBuckRuleComposer
import com.uber.okbuck.core.model.AndroidAppTarget

final class AndroidBinaryRuleComposer extends AndroidBuckRuleComposer {
    static AndroidBinaryRule compose(
            AndroidAppTarget target,
            List<String> deps,
            String manifest,
            String keystore) {
        return new AndroidBinaryRule(
                bin(target),
                ["//visibility:public"],
                deps,
                manifest,
                keystore,
                target.proguardConfig,
                target.placeholders)
    }
}