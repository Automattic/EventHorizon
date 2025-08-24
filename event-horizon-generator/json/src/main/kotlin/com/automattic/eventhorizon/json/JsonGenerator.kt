package com.automattic.eventhorizon.json

import com.automattic.eventhorizon.CaseString
import com.automattic.eventhorizon.Event as InputEvent
import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property as InputProperty
import com.automattic.eventhorizon.PropertyType
import com.automattic.eventhorizon.Schema as InputSchema
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isDirectory
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

  override fun generate(schema: InputSchema, outputPath: Path): Path {
    val outputEvents = schema.events.map(InputEvent::toEvent)
    val outputSchema = Schema(
      platforms = schema.platforms.map(Platform::value).sorted(),
      events = outputEvents.sortedBy(Event::name),
    )
    val jsonText = json.encodeToString(outputSchema)

    val outputFile = if (outputPath.isDirectory()) {
      outputPath.resolve("event-horizon.json")
    } else {
      outputPath
    }
    outputFile.parent.createDirectories()
    outputFile.writeText(jsonText)
    return outputFile
  }
}

private fun InputEvent.toEvent() = Event(
  key = name.rawValue,
  name = name.toHumanReadableString(uppercaseWords = true),
  description = description,
  excludedPlatforms = excludedPlatforms.map(Platform::value),
  properties = properties
    .map(InputProperty::toProperty)
    .sortedWith(compareBy({ it.optionalPlatforms.isNotEmpty() }, Property::key)),
)

private fun InputProperty.toProperty() = Property(
  key = name.rawValue,
  description = description,
  type = when (type) {
    is PropertyType.Text -> "text"
    is PropertyType.Boolean -> "boolean"
    is PropertyType.Number -> "number"
    is PropertyType.Enum -> "enum"
  },
  values = (type as? PropertyType.Enum)?.values?.map(CaseString::rawValue).orEmpty(),
  optionalPlatforms = optionalPlatforms.map(Platform::value),
)

@Serializable
private class Schema(
  val platforms: List<String>,
  val events: List<Event>,
)

@Serializable
private class Event(
  val key: String,
  val name: String,
  val description: String?,
  val excludedPlatforms: List<String>,
  val properties: List<Property>,
)

@Serializable
private class Property(
  val key: String,
  val description: String?,
  val type: String,
  val values: List<String>,
  val optionalPlatforms: List<String>,
)
