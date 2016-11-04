package com.uber.okbuck.bazel

import com.uber.okbuck.rule.BuckRule

final class AndroidLibraryRule extends BuckRule {
    private final Set<String> mSrcTargets
    private final Set<String> mSrcSet
    private final String mManifest
    private final Set<String> mProvidedDeps
    private final String mResourcesDir

    AndroidLibraryRule(
            String name,
            List<String> visibility,
            List<String> deps,
            Set<String> srcSet,
            String manifest,
            Set<String> providedDeps,
            String resourcesDir) {
        super("android_library", name, visibility, deps)
        mSrcSet = srcSet
        mManifest = manifest
        mProvidedDeps = providedDeps
        mResourcesDir = resourcesDir
    }

    @Override
    protected final void printContent(PrintStream printer) {
        if (mSrcTargets != null && mSrcTargets.empty) {
            printer.println("\tsrcs = glob([")
        } else {
            printer.println("\tsrcs = [")
            for (String target : mSrcTargets) {
                printer.println("\t\t'${target}',")
            }
            printer.println("\t] + glob([")
        }
        for (String src : mSrcSet) {
            printer.println("\t\t'${src}/**/*.java',")
        }

        if (mResourcesDir) {
            printer.println("\tresources = glob([")
            printer.println("\t\t'${mResourcesDir}/**',")
            printer.println("\t]),")

            printer.println("\tresources_root = '${mResourcesDir}',")
        }

        if (mManifest != null && !mManifest.isEmpty()) {
            printer.println("\tmanifest = '${mManifest}',")
        }

        if (!mProvidedDeps.empty) {
            printer.println("\tprovided_deps = [")
            for (String dep : mProvidedDeps.sort()) {
                printer.println("\t\t'${dep}',")
            }
            printer.println("\t],")
        }
    }

}
