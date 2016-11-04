package com.uber.okbuck.bazel

import com.uber.okbuck.core.util.FileUtil
import com.uber.okbuck.core.dependency.DependencyCache
import org.gradle.api.Project

final class BazelDependencyCache extends DependencyCache {

    BazelDependencyCache(Project rootProject, String cacheDirPath) {
        super(rootProject, cacheDirPath, true, false, true)
        File cacheBuildFile = new File(cacheDir.parent, "BUILD")
        if (!cacheBuildFile.exists()) {
            FileUtil.copyResourceToProject("bazel/BUILD", cacheBuildFile)
        }
        File cacheBzlFile = new File(cacheDir.parent, "create_imports.bzl")
        if (!cacheBzlFile.exists()) {
            FileUtil.copyResourceToProject("bazel/create_imports.bzl", cacheBzlFile)
        }
    }
}
