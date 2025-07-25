package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.EventHorizonSchema
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Platform
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

public class TypeScriptGenerator(
  private val platform: Platform,
) : Generator {
  override fun generate(schema: EventHorizonSchema, outputDir: Path): Path {
    val platformEvents = Events(
      schema.events.filter { platform == Platform.NoPlatform || platform in it.availablePlatforms },
    )
    val trackableType = TrackableType(platformEvents, platform)
    val enumTypes = platformEvents.distinctEnums.map(::EventPropertyType)

    val tsText = buildString {
      append(trackableType.typeSpec)
      appendNewLine()
      enumTypes.forEachIndexed { index, enum ->
        appendNewLine()
        append(enum.typeSpec)
        appendNewLine()
      }
    }

    val outputPath = outputDir.resolve("eventHorizon.ts")
    outputPath.parent.createDirectories()
    outputPath.writeText(tsText)
    return outputPath
  }
}
