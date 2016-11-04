package com.uber.okbuck.bazel

import com.uber.okbuck.rule.BuckRule

final class AndroidBinaryRule extends BuckRule {

    private final String manifest
    private final String proguardConfig
    private final Map<String, Object> placeholders

    AndroidBinaryRule(
            String name,
            List<String> visibility,
            List<String> deps,
            String manifest,
            String proguardConfig,
            Map<String, Object> placeholders) {
        super("android_binary", name, visibility, deps)

        this.manifest = manifest
        this.proguardConfig = proguardConfig
        this.placeholders = placeholders
    }

    @Override
    protected final void printContent(PrintStream printer) {
        if (manifest != null && !manifest.isEmpty()) {
            printer.println("\tmanifest = '${manifest}',")
        }
        if (!placeholders.isEmpty()) {
            printer.println("\tmanifest_values = {")
            placeholders.each { key, value ->
                printer.println("\t\t'${key}': '${value}',")
            }
            printer.println("\t},")
        }
    }
}
