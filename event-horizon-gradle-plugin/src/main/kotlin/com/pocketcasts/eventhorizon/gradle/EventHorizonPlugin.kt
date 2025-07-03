package com.pocketcasts.eventhorizon.gradle

import com.android.build.api.variant.AndroidComponentsExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

public class EventHorizonPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    val extension = target.extensions.create(PluginName, EventHorizonExtension::class.java)
    target.checkKotlinPlugin()
    target.registerCodeGenTask(extension)
  }

  private fun Project.registerCodeGenTask(extension: EventHorizonExtension) {
    val task = tasks.register("generateEventHorizon", CodeGenTask::class.java) { task ->
      task.packageName.set(extension.packageName)
      task.inputFile.set(extension.inputFile)
    }
    makeKotlinDependOnTask(task)
    contributeToSourceSets(task)
  }

  private fun Project.makeKotlinDependOnTask(task: TaskProvider<out Task>) {
    tasks.withType(KotlinCompile::class.java).configureEach { kotlinTask ->
      kotlinTask.dependsOn(task)
    }
  }

  private fun Project.contributeToSourceSets(task: TaskProvider<CodeGenTask>) =
    if (plugins.hasPlugin("com.android.base")) {
      contributeToAndroid(task)
    } else {
      contributeToKotlin(task)
    }

  private fun Project.contributeToKotlin(task: TaskProvider<CodeGenTask>) {
    val sourceSets = extensions.getByType(KotlinSourceSetContainer::class.java).sourceSets
    val kotlinSourceSet = sourceSets.getByName("main").kotlin
    kotlinSourceSet.srcDir(task)
  }

  private fun Project.contributeToAndroid(task: TaskProvider<CodeGenTask>) {
    extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
      // 'kotlin' sources aren't automatically included in a project
      variant.sources.java?.addGeneratedSourceDirectory(task, CodeGenTask::outputDir)
    }
  }

  private fun Project.checkKotlinPlugin() {
    val hasKotlin = with(plugins) {
      hasPlugin("org.jetbrains.kotlin.jvm") || hasPlugin("org.jetbrains.kotlin.android")
    }
    check(hasKotlin) {
      "EventHorizon Gradle plugin applied in '$path' requires Kotlin plugin."
    }
  }
}
