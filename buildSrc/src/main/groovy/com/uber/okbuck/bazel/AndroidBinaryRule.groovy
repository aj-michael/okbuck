package com.uber.okbuck.bazel

import com.uber.okbuck.rule.BuckRule

final class AndroidBinaryRule extends BuckRule {

    private final String mManifest
    private final String mKeystore
    private final String mProguardConfig
    private final Map<String, Object> mPlaceholders

    AndroidBinaryRule(
            String name,
            List<String> visibility,
            List<String> deps,
            String manifest,
            String keystore,
            String proguardConfig,
            Map<String, Object> placeholders) {
        super("android_binary", name, visibility, deps)

        mManifest = manifest
        mKeystore = keystore
        mProguardConfig = proguardConfig
        mPlaceholders = placeholders
    }

    @Override
    protected final void printContent(PrintStream printer) {
        printer.println("\tmanifest = '${mManifest}',")
        printer.println("\tkeystore = '${mKeystore}',")
        if (!mPlaceholders.isEmpty()) {
            printer.println("\tmanifest_entries = {")
            printer.println("\t\t'placeholders': {")
            mPlaceholders.each { key, value ->
                printer.println("\t\t\t'${key}': '${value}',")
            }
            printer.println("\t\t},")
            printer.println("\t},")
        }
    }
}
