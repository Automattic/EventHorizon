package com.pocketcasts.eventhorizon.kotlin

import com.pocketcasts.eventhorizon.Events
import com.pocketcasts.eventhorizon.Generator
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Path

public class KotlinGenerator(
  private val packageName: String,
) : Generator {
  override fun generate(events: Events, outputDir: Path): Path {
    val trackable = TrackableInterface(packageName)
    val eventHorizonType = EventHorizonClass(packageName, trackable).typeSpec
    val eventTypes = events.map { event -> EventClass(packageName, event, trackable).typeSpec }
    val enumTypes = events.distinctEnums.map { enum -> EventPropertyEnum(packageName, enum).typeSpec }

    return FileSpec
      .builder(packageName, "EventHorizon")
      .addType(eventHorizonType)
      .addType(trackable.typeSpec)
      .addTypes(eventTypes)
      .addTypes(enumTypes)
      .build()
      .writeTo(outputDir)
  }
}
