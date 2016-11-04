package com.uber.okbuck.bazel

final class JavaLibraryRule extends JavaRule {

    JavaLibraryRule(
            String name,
            List<String> visibility,
            List<String> deps,
            Set<String> srcSet,
            String resourcesDir) {

        super(
                "java_library",
                name,
                visibility,
                deps,
                srcSet,
                new HashSet<String>(),
                resourcesDir)
    }
}