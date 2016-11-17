package com.uber.okbuck.bazel

import com.google.common.collect.Iterables
import com.uber.okbuck.core.dependency.DependencyCache
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.internal.impldep.com.google.common.collect.ImmutableCollection
import org.gradle.internal.impldep.com.google.common.collect.ImmutableSortedSet
import org.gradle.internal.impldep.com.google.common.collect.Ordering

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
            writeCacheBuildFile(rootProject, new File(cacheDir.parent, "BUILD"))
        }
    }

    private static writeCacheBuildFile(Project rootProject, File buildFile) {
        buildFile.append """package(default_visibility = ["//visibility:public"])\n\n"""
        Set<String> visitedDependencies = new HashSet<>()
        rootProject.okbuck.buckProjects.each { Project project ->
            project.configurations.getAsMap().each { String name, Configuration config ->
                if (name.toLowerCase().endsWith("compile")) {
                    config.resolvedConfiguration.getFirstLevelModuleDependencies().each {
                        ResolvedDependency resolvedDependency ->
                            writeBuildRules(resolvedDependency, buildFile, visitedDependencies)
                    }
                }
            }
        }
    }

    private static String stripResolvedDependencyScope(ResolvedDependency resolvedDependency) {
        return resolvedDependency.toString().split(";")[0]
    }

    private static writeBuildRules(
            ResolvedDependency resolvedDependency,
            File buildFile,
            Set<String> visitedDependencies) {
        if (visitedDependencies.contains(stripResolvedDependencyScope(resolvedDependency))) {
            return
        } else {
            visitedDependencies.add(stripResolvedDependencyScope(resolvedDependency))
        }
        Set<ResolvedArtifact> artifacts = []
        resolvedDependency.parents.each { ResolvedDependency parent ->
            artifacts += resolvedDependency.getArtifacts(parent)
        }
        ResolvedArtifact artifact = Iterables.getOnlyElement(artifacts)
        if (artifact.id.componentIdentifier.displayName.contains(" ")) {
            // This is a local dependency. It is not in the cache.
            return
        }
        if (artifact.getExtension().endsWith("aar")) {
            buildFile.append """aar_import(
    name = '${getRuleName(artifact)}',
    aar = 'cache/${getRuleName(artifact)}',
    exports = [
"""
        } else if (artifact.getExtension().endsWith("jar")) {
            buildFile.append """java_import(
    name = '${getRuleName(artifact)}',
    jars = ['cache/${getRuleName(artifact)}'],
    exports = [
"""
        }
        resolvedDependency.children.collect { child ->
            getRuleName(Iterables.getOnlyElement(child.getArtifacts(resolvedDependency)))
        }.unique().sort().each { String exportName ->
            buildFile.append "        ':${exportName}',\n"
        }
        buildFile.append "    ],\n)\n\n"

        resolvedDependency.children.each { ResolvedDependency child ->
            writeBuildRules(child, buildFile, visitedDependencies)
        }
    }

    private static String getRuleName(ResolvedArtifact artifact) {
        String displayName = artifact.id.componentIdentifier.displayName
        return displayName.replaceFirst(":", ".").replace(":", "-") + ".${artifact.getExtension()}"
    }
}
