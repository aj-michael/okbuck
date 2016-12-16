package com.uber.okbuck.wrapper

import com.uber.okbuck.core.util.FileUtil
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

class BuckWrapperTask extends DefaultTask {

    @Input
    String repo

    @Input
    List<String> remove

    @Input
    List<String> keep

    @Input
    List<String> watch

    @Input
    List<String> sourceRoots

    @Input
    File wrapperFile

    @Input
    String wrapperTemplate

    @TaskAction
    void installWrapper() {
        FileUtil.copyResourceToProject(wrapperTemplate, wrapperFile)

        String outputText = wrapperFile.text
        outputText = outputText
                .replaceFirst('template-creation-time', new Date().toString())
                .replaceFirst('template-custom-buck-repo', repo)
                .replaceFirst('template-remove', toWatchmanMatchers(remove))
                .replaceFirst('template-keep', toWatchmanMatchers(keep))
                .replaceFirst('template-watch', toWatchmanMatchers(watch))
                .replaceFirst('template-source-roots', toWatchmanMatchers(sourceRoots))

        wrapperFile.text = outputText
        wrapperFile.setExecutable(true)
    }

    static String toWatchmanMatchers(List<String> wildcardPatterns) {
        return wildcardPatterns.collect { pattern ->
            "            [\"imatch\", \"${pattern}\", \"wholename\"]"
        }.join(",\n")
    }
}
