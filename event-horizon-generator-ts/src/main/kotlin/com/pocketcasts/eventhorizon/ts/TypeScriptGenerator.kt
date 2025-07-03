package com.pocketcasts.eventhorizon.ts

import com.pocketcasts.eventhorizon.Events
import com.pocketcasts.eventhorizon.Generator
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

public class TypeScriptGenerator : Generator {
  override fun generate(events: Events, outputDir: Path): Path {
    val trackableType = TrackableType(events)
    val enumTypes = events.distinctEnums.map(::EventPropertyType)

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
