package com.automattic.eventhorizon.json

import com.automattic.eventhorizon.CaseString
import com.automattic.eventhorizon.Event as InputEvent
import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Group as InputGroup
import com.automattic.eventhorizon.Platform as InputPlatform
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
    val outputSchema = Schema(
      platforms = schema.platforms.toPlatforms(),
      groups = schema.groups.toGroups(),
      events = schema.events.toEvents(),
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

private fun Set<InputPlatform>.toPlatforms() = map(InputPlatform::value).sorted()

private fun List<InputGroup>.toGroups() = buildList {
  val regularGroups = this@toGroups.minus(InputGroup.empty)
    .map(InputGroup::toGroup)
    .sortedBy(Group::key)
  addAll(regularGroups)
  add(InputGroup.empty.toGroup())
}

private fun InputGroup.toGroup() = Group(
  key = key.rawValue,
  name = name,
  description = description,
)

private fun List<InputEvent>.toEvents() = buildMap {
  val sortedMap = groupBy { event -> event.groupKey.rawValue }
    .mapValues { (_, events) -> events.map(InputEvent::toEvent).sortedBy(Event::name) }
    .toSortedMap()
  val ungroupedKey = InputGroup.empty.key.rawValue
  val ungrouped = sortedMap.remove(ungroupedKey)
  putAll(sortedMap)
  if (ungrouped != null) {
    put(ungroupedKey, ungrouped)
  }
}

private fun InputEvent.toEvent() = Event(
  key = name.rawValue,
  name = name.toHumanReadableString(uppercaseWords = true),
  description = description,
  excludedPlatforms = excludedPlatforms.toPlatforms(),
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
  optionalPlatforms = optionalPlatforms.toPlatforms(),
)

@Serializable
private class Schema(
  val platforms: List<String>,
  val groups: List<Group>,
  val events: Map<String, List<Event>>,
)

@Serializable
private class Group(
  val key: String,
  val name: String,
  val description: String?,
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
