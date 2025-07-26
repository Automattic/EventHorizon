package com.automattic.eventhorizon.json

import com.automattic.eventhorizon.Event as InputEvent
import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property as InputProperty
import com.automattic.eventhorizon.PropertyType
import com.automattic.eventhorizon.Schema
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

public class JsonGenerator(
  prettyPrint: Boolean = false,
) : Generator {
  private val json = Json {
    explicitNulls = false
    this.prettyPrint = prettyPrint
  }

  override fun generate(schema: Schema, outputDir: Path): Path {
    val outputEvents = schema.events.map(InputEvent::toEvent)
    val jsonText = json.encodeToString(outputEvents)

    val outputPath = outputDir.resolve("event-horizon.json")
    outputPath.parent.createDirectories()
    outputPath.writeText(jsonText)
    return outputPath
  }
}

private fun InputEvent.toEvent() = Event(
  name = name,
  documentation = documentation,
  excludedPlatforms = excludedPlatforms.map(Platform::value),
  properties = properties.map(InputProperty::toProperty),
)

private fun InputProperty.toProperty() = Property(
  name = name,
  documentation = documentation,
  type = when (type) {
    is PropertyType.Text -> "text"
    is PropertyType.Boolean -> "boolean"
    is PropertyType.Number -> "number"
    is PropertyType.Enum -> "enum"
  },
  values = (type as? PropertyType.Enum)?.values?.toList().orEmpty(),
  optionalPlatforms = optionalPlatforms.map(Platform::value),
)

@Serializable
private class Event(
  val name: String,
  val documentation: String?,
  val excludedPlatforms: List<String>,
  val properties: List<Property>,
)

@Serializable
private class Property(
  val name: String,
  val documentation: String?,
  val type: String,
  val values: List<String>,
  val optionalPlatforms: List<String>,
)
