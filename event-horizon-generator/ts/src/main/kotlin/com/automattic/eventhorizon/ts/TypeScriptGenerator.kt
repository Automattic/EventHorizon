package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Schema
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

public class TypeScriptGenerator(
  private val platform: Platform,
) : Generator {
  override fun generate(schema: Schema, outputPath: Path): Path {
    val platformEvents = schema.platformEvents(platform)
    val trackableType = TrackableType(platformEvents, platform)
    val enumTypes = schema.platformEnums(platform).map(::EventPropertyType)

    val tsText = buildString {
      append(trackableType.typeSpec)
      appendNewLine()
      enumTypes.forEachIndexed { index, enum ->
        appendNewLine()
        append(enum.typeSpec)
        appendNewLine()
      }
    }

    val outputFile = outputPath.resolve("eventHorizon.ts")
    outputFile.parent.createDirectories()
    outputFile.writeText(tsText)
    return outputFile
  }
}
