package com.uber.okbuck.bazel

import com.uber.okbuck.rule.BuckRule

class JavaLibraryRule extends BuckRule {

    private final Set<String> srcSet
    private final String resourcesDir
    private final Set<String> providedDeps
    private final Set<String> deps

    JavaLibraryRule(
            String ruleType,
            String name,
            List<String> visibility,
            List<String> deps,
            Set<String> srcSet,
            String resourcesDir) {
        super(ruleType, name, visibility)
        this.srcSet = srcSet
        this.resourcesDir = resourcesDir
        this.providedDeps = providedDeps
        this.deps = deps
    }

    @Override
    protected final void printContent(PrintStream printer) {
        if (!srcSet.empty) {
            printer.println("\tsrcs = glob([")
            for (String src : srcSet) {
                printer.println("\t\t'${src}/**/*.java',")
            }
            printer.println("\t]),")
            printer.println("\tdeps = [")
            for (String dep : deps) {
                printer.println("\t\t'${dep}',")
            }
            printer.println("\t],")
        } else {
            printer.println("\truntime_deps = [")
            for (String dep : deps) {
                printer.println("\t\t'${dep}',")
            }
            printer.println("\t],")
        }

        if (resourcesDir) {
            printer.println("\tresources = glob([")
            printer.println("\t\t'${resourcesDir}/**',")
            printer.println("\t]),")
        }

        if (!providedDeps.empty) {
            printer.println("\texports = [")
            for (String dep : providedDeps.sort()) {
                printer.println("\t\t'${dep}',")
            }
            printer.println("\t],")
        }
    }
}
