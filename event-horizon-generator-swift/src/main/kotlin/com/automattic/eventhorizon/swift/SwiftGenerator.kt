package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Schema
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.TypeSpec
import java.nio.file.Path

public class SwiftGenerator(
  private val moduleName: String,
  private val platform: Platform,
) : Generator {
  override fun generate(schema: Schema, outputDir: Path): Path {
    val platformEvents = schema.platformEvents(platform)
    val trackable = TrackableProtocol(moduleName)
    val eventHorizonType = EventHorizonClass(moduleName, trackable).typeSpec
    val eventTypes = platformEvents.map { event -> EventStruct(moduleName, event, trackable, platform).typeSpec }
    val enumTypes = platformEvents.distinctEnums.map { enum -> EventPropertyEnum(moduleName, enum).typeSpec }

    val fileSpec = FileSpec
      .builder(moduleName, "EventHorizon")
      .addType(eventHorizonType)
      .addType(trackable.typeSpec)
      .addTypes(eventTypes)
      .addTypes(enumTypes)
      .build()
    fileSpec.writeTo(outputDir)
    return outputDir.resolve("${fileSpec.name}.swift")
  }
}

private fun FileSpec.Builder.addTypes(types: Iterable<TypeSpec>) = apply { types.forEach(::addType) }
