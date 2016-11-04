package com.uber.okbuck.composer

import com.uber.okbuck.core.model.AndroidAppTarget
import com.uber.okbuck.rule.AndroidBinaryRule

final class AndroidBinaryRuleComposer extends AndroidBuckRuleComposer {

    private static Map<String, String> CPU_FILTER_MAP = [
            "armeabi"    : "ARM",
            "armeabi-v7a": "ARMV7",
            "x86"        : "X86",
            "x86_64"     : "X86_64",
            "mips"       : "MIPS",
    ]

    private AndroidBinaryRuleComposer() {
        // no instance
    }

    static AndroidBinaryRule compose(AndroidAppTarget target, List<String> deps) {
        Set<String> mappedCpuFilters = target.cpuFilters.collect { String cpuFilter ->
            CPU_FILTER_MAP.get(cpuFilter)
        }.findAll { String cpuFilter -> cpuFilter != null }
        return new AndroidBinaryRule(
                bin(target),
                ["//visibility:public"],
                deps,
                target.manifest,
                [],
                target.main.sources
        )
    }
}
