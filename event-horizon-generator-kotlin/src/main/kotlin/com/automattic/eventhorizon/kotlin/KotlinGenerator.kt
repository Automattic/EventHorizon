package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Schema
import com.squareup.kotlinpoet.FileSpec
import java.nio.file.Path

public class KotlinGenerator(
  private val packageName: String,
  private val platform: Platform,
) : Generator {
  override fun generate(schema: Schema, outputDir: Path): Path {
    val platformEvents = schema.platformEvents(platform)
    val trackable = TrackableInterface(packageName)
    val eventHorizonType = EventHorizonClass(packageName, trackable).typeSpec
    val eventTypes = platformEvents.map { event -> EventClass(packageName, event, trackable, platform).typeSpec }
    val enumTypes = platformEvents.distinctEnums.map { enum -> EventPropertyEnum(packageName, enum).typeSpec }

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
