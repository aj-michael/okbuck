package com.uber.okbuck.bazel

import com.uber.okbuck.rule.BuckRule

final class JavaBinaryRule extends BuckRule {

    private final String mainClass

    JavaBinaryRule(String name, List<String> visibility, List<String> deps, String mainClass) {
        super("java_binary", name, visibility, deps)
        this.mainClass = mainClass
    }

    @Override
    protected final void printContent(PrintStream printer) {
        if (mainClass) {
            printer.println("\tmain_class = '${mainClass}',")
        }
    }
}
