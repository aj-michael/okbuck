package com.uber.okbuck.bazel

import com.uber.okbuck.rule.BuckRule

final class AndroidLibraryRule extends BuckRule {
    private final Set<String> srcTargets
    private final Set<String> srcSet
    private final String manifest
    private final Set<String> providedDeps
    private final String resourcesDir

    AndroidLibraryRule(
            String name,
            List<String> visibility,
            List<String> deps,
            Set<String> srcSet,
            String manifest,
            Set<String> providedDeps,
            String resourcesDir) {
        super("android_library", name, visibility, deps)
        this.srcSet = srcSet
        this.manifest = manifest
        this.providedDeps = providedDeps
        this.resourcesDir = resourcesDir
    }

    @Override
    protected final void printContent(PrintStream printer) {
        if (srcTargets != null && srcTargets.empty) {
            printer.println("\tsrcs = glob([")
        } else {
            printer.println("\tsrcs = [")
            for (String target : srcTargets) {
                printer.println("\t\t'${target}',")
            }
            printer.println("\t] + glob([")
        }
        for (String src : srcSet) {
            printer.println("\t\t'${src}/**/*.java',")
        }
        printer.println("\t]),")

        if (resourcesDir) {
            printer.println("\tresources = glob([")
            printer.println("\t\t'${resourcesDir}/**',")
            printer.println("\t]),")

            printer.println("\tresources_root = '${resourcesDir}',")
        }

        if (manifest != null && !manifest.isEmpty()) {
            printer.println("\tmanifest = '${manifest}',")
        }

        if (!providedDeps.empty) {
            printer.println("\tprovided_deps = [")
            for (String dep : providedDeps.sort()) {
                printer.println("\t\t'${dep}',")
            }
            printer.println("\t],")
        }
    }

}
