package com.uber.okbuck.bazel

import com.uber.okbuck.core.dependency.DependencyCache
import org.gradle.api.Project

final class BazelDependencyCache extends DependencyCache {

    // The okbuck cache contains a BUCK file at `cacheDirPath/BUCK`. We cannot put a BUILD file
    // there, because the JARs and AARs are also in that directory so we cannot create java_import
    // and aar_import rules with the same names. Instead we put the BUILD file in `cacheDirPath/..`.
    static Set<String> external(Set<String> deps) {
        return deps.collect { String dep -> "//okbazel:${dep.tokenize('/').last()}" }
    }

    BazelDependencyCache(Project rootProject, String cacheDirPath) {
        super(rootProject, cacheDirPath, true, false, true)
        File cacheBuildFile = new File(cacheDir.parent, "BUILD")
        if (!cacheBuildFile.exists()) {
            cacheBuildFile.write """package(default_visibility = ["//visibility:public"])
load("//okbazel:create_imports.bzl", "create_imports")
create_imports()
"""
        }
        File cacheBzlFile = new File(cacheDir.parent, "create_imports.bzl")
        if (!cacheBzlFile.exists()) {
            cacheBzlFile.write """def create_imports():
  for jar in native.glob(['cache/*.jar']):
    native.java_import(
        name = jar.split("/")[1],
        jars = [jar],
    )
  for aar in native.glob(['cache/*.aar']):
    native.aar_import(
        name = aar.split("/")[1],
        aar = aar,
    )
"""
        }
    }
}
