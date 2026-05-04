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
  override fun generate(schema: Schema, outputPath: Path): Path {
    val platformEvents = schema.platformEvents(platform)
    val eventStruct = EventStruct(moduleName)
    val eventHorizonType = EventHorizonClass(moduleName, eventStruct).typeSpec
    val eventStructExtension = EventStructExtension(moduleName, eventStruct, platformEvents, platform).extensionSpec
    val enumTypes = schema.platformEnums(platform).map { enum -> EventPropertyEnum(moduleName, enum).typeSpec }
    val hasEnums = enumTypes.isNotEmpty()

    val fileSpec = FileSpec
      .builder(moduleName, "EventHorizon")
      .addType(eventHorizonType)
      .addType(eventStruct.typeSpec)
      .addExtension(eventStructExtension)
      .also { builder ->
        if (hasEnums) {
          builder.addType(AnalyticsValueProtocol.typeSpec)
          builder.addExtension(AnalyticsValueProtocol.extensionSpec)
        }
      }
      .addTypes(enumTypes)
      .build()
    fileSpec.writeTo(outputPath)
    return outputPath.resolve("${fileSpec.name}.swift")
  }
}

private fun FileSpec.Builder.addTypes(types: Iterable<TypeSpec>) = apply { types.forEach(::addType) }
