package com.uber.okbuck.bazel

import com.uber.okbuck.rule.BuckRule

abstract class JavaRule extends BuckRule {

    private final Set<String> mSrcSet
    private final String mResourcesDir
    private final Set<String> mProvidedDeps

    JavaRule(
            String ruleType,
            String name,
            List<String> visibility,
            List<String> deps,
            Set<String> srcSet,
            Set<String> providedDeps,
            String resourcesDir) {

        super(ruleType, name, visibility, deps)
        mSrcSet = srcSet
        mResourcesDir = resourcesDir
        mOptions = options
        mProvidedDeps = providedDeps
        mLabels = labels
    }

    @Override
    protected final void printContent(PrintStream printer) {
        if (!mSrcSet.empty) {
            printer.println("\tsrcs = glob([")
            for (String src : mSrcSet) {
                printer.println("\t\t'${src}/**/*.java',")
            }
            printer.println("\t]),")
        }

        if (mResourcesDir) {
            printer.println("\tresources = glob([")
            printer.println("\t\t'${mResourcesDir}/**',")
            printer.println("\t]),")

            printer.println("\tresources_root = '${mResourcesDir}',")
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
