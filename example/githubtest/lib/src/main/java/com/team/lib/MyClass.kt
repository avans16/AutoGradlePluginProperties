package com.team.lib

import com.team.autoplugin.AutoPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

@AutoPlugin(Plugin::class)
class MyClass : Plugin<Project> {
    override fun apply(p0: Project) {
       
    }
}