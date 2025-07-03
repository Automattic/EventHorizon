package com.pocketcasts.eventhorizon.gradle

import com.pocketcasts.eventhorizon.kotlin.KotlinGenerator
import com.pocketcasts.eventhorizon.parseEvents
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class CodeGenTask @Inject constructor(
  objects: ObjectFactory,
) : DefaultTask() {
  @get:Input
  @get:Optional
  public val packageName: Property<String> = objects.property(String::class.java)

  @get:InputFile
  @get:Optional
  @get:PathSensitive(PathSensitivity.NONE)
  public val inputFile: RegularFileProperty = objects.fileProperty()

  @get:OutputDirectory
  public val outputDir: DirectoryProperty = objects
    .directoryProperty()
    .convention(project.layout.buildDirectory.dir("generated/event-horizon"))

  override fun getGroup(): String = PluginName

  override fun getDescription(): String = "Generate analytic events and their definitions"

  @TaskAction
  internal fun generateCode() {
    val inputFile = inputFile.orNull?.asFile?.toPath()
    if (inputFile != null) {
      val outputDir = outputDir.get().asFile
      outputDir.deleteRecursively()
      val events = parseEvents(inputFile).getOrThrow()
      KotlinGenerator(packageName.getOrElse("")).generate(events, outputDir.toPath())
    }
  }
}
