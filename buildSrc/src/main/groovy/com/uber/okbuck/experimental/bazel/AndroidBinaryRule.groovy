package com.uber.okbuck.experimental.bazel

import com.uber.okbuck.rule.BuckRule

final class AndroidBinaryRule extends BuckRule {
    private static final String RULE_TYPE = "android_binary"

    private final String packageName
    private final String manifest
    private final Map<String, Object> placeholders

    AndroidBinaryRule(
            String name,
            String packageName,
            String androidLibrary,
            String manifest,
            Map<String, Object> placeholders) {
        super(RULE_TYPE, name, [], [androidLibrary])
        this.packageName = packageName
        this.manifest = manifest
        this.placeholders = placeholders
    }

    @Override
    protected final void printContent(PrintStream printer) {
        printer.println("\tcustom_package = '${packageName}',")
        if (manifest != null && !manifest.isEmpty()) {
            printer.println("\tmanifest = '${manifest}',")
        }
        if (!placeholders.isEmpty()) {
            printer.println("\tmanifest_values = {")
            placeholders.each { key, value -> printer.println("\t\t'${key}': '${value}',") }
            printer.println("\t},")
        }
    }
}
