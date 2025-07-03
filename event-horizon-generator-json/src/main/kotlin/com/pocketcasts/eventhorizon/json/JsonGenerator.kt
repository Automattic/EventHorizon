package com.pocketcasts.eventhorizon.json

import com.pocketcasts.eventhorizon.Event as InputEvent
import com.pocketcasts.eventhorizon.Events
import com.pocketcasts.eventhorizon.Generator
import com.pocketcasts.eventhorizon.Platform
import com.pocketcasts.eventhorizon.Property as InputProperty
import com.pocketcasts.eventhorizon.Property.Type
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

  override fun generate(events: Events, outputDir: Path): Path {
    val outputEvents = events.map(InputEvent::toEvent)
    val jsonText = json.encodeToString(outputEvents)

    val outputPath = outputDir.resolve("event-horizon.json")
    outputPath.parent.createDirectories()
    outputPath.writeText(jsonText)
    return outputPath
  }
}

private fun InputEvent.toEvent() = Event(
  name = name,
  description = description,
  properties = properties.map(InputProperty::toProperty),
)

private fun InputProperty.toProperty() = Property(
  name = name,
  description = description,
  type = when (type) {
    is Type.Text -> "text"
    is Type.Boolean -> "boolean"
    is Type.Number -> "number"
    is Type.Enum -> "enum"
  },
  values = (type as? Type.Enum)?.values?.toList().orEmpty(),
  optionalPlatforms = optionalPlatforms.map { platform ->
    when (platform) {
      Platform.Android -> "android"
      Platform.Ios -> "ios"
      Platform.Web -> "web"
    }
  },
)

@Serializable
private class Event(
  val name: String,
  val description: String?,
  val properties: List<Property>,
)

@Serializable
private class Property(
  val name: String,
  val description: String?,
  val type: String,
  val values: List<String>,
  val optionalPlatforms: List<String>,
)
