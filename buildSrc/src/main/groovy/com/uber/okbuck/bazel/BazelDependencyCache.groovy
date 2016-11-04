package com.uber.okbuck.bazel

import com.uber.okbuck.core.dependency.DependencyCache
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

final class BazelDependencyCache extends DependencyCache {

    BazelDependencyCache(Project rootProject, String cacheDirPath) {
        super(rootProject, cacheDirPath, true, false, true)
        File cacheBuildFile = new File(cacheDir.parent, "BUILD")
        if (!cacheBuildFile.exists()) {
            FileUtils.copyURLToFile(
                    this.class.getClassLoader().getResource("com/uber/okbuck/bazel/BUILD"),
                    cacheBuildFile)
        }
        File cacheBzlFile = new File(cacheDir.parent, "create_imports.bzl")
        if (!cacheBzlFile.exists()) {
            FileUtils.copyURLToFile(
                    this.class.getClassLoader().getResource("com/uber/okbuck/bazel/create_imports.bzl"),
                    cacheBzlFile)
        }
    }
}
