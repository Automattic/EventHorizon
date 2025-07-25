package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.EventHorizonSchema
import com.automattic.eventhorizon.Generator
import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.TypeSpec
import java.nio.file.Path

public class SwiftGenerator(
  private val moduleName: String,
) : Generator {
  override fun generate(schema: EventHorizonSchema, outputDir: Path): Path {
    val trackable = TrackableProtocol(moduleName)
    val eventHorizonType = EventHorizonClass(moduleName, trackable).typeSpec
    val eventTypes = schema.events.map { event -> EventStruct(moduleName, event, trackable).typeSpec }
    val enumTypes = schema.events.distinctEnums.map { enum -> EventPropertyEnum(moduleName, enum).typeSpec }

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
