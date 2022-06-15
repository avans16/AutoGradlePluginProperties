package com.team.groovyhub

import com.team.autoplugin.AutoPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.tooling.provider.model.ToolingModelBuilder
import org.gradle.tooling.provider.model.ToolingModelBuilderRegistry

@AutoPlugin(id = "com.team.android.plugin", value = Plugin.class)
class beanPlugin implements Plugin<Project> {

    public beanPlugin(ToolingModelBuilderRegistry registry) {

        registry.register(new buildmode())
    }

    void apply(Project project) {
        project.tasks.create('myhello', GreetingTask)
        //  Files.asCharSink()
    }
}

class buildmode implements ToolingModelBuilder {

    @Override
    boolean canBuild(String modelName) {
        return true
    }

    @Override
    Object buildAll(String modelName, Project project) {
        return new CustomModel()
    }
}

class CustomModel {
    boolean hasPlugin() {
        return true
    }
}