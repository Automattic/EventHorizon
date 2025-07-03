package com.pocketcasts.eventhorizon

import com.charleskorn.kaml.Yaml as YamlObject
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.yamlScalar
import com.pocketcasts.eventhorizon.Property.Type
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlinx.serialization.Serializable

private const val DescriptionNode = "description"
private val Yaml = YamlObject.default

public fun parseEvents(file: Path): Result<Events> = runCatching {
  if (file.fileSize() != 0L) {
    parseFile(file)
  } else {
    Events()
  }
}

private fun parseFile(file: Path): Events {
  val definition = Yaml.decodeFromStream<InputDefinition>(file.inputStream())
  val enums = definition.enums.map { (name, values) -> Type.Enum(name, values.orEmpty()) }
  val events = definition
    .parseProperties(enums)
    .map { (eventName, description, properties) -> Event(eventName, description, properties) }
  return Events(events)
}

private fun InputDefinition.parseProperties(enums: List<Type.Enum>) = events.map { (eventName, rawProperties) ->
  val description = rawProperties?.parseDescription()
  val properties = rawProperties
    ?.minus(DescriptionNode)
    ?.mapValues { (_, rawProperty) -> Yaml.decodeFromYamlNode<EventPropertyConfiguration>(rawProperty) }
    ?.parseProperties(enums)
    .orEmpty()
  Triple(eventName, description, properties)
}

private fun Map<String, YamlNode>.parseDescription() = when (val yamlDescription = get(DescriptionNode)) {
  is YamlScalar -> yamlDescription.content
  is YamlNull, null -> null
  else -> throw YamlException("'description' cannot be used as a property name", yamlDescription.path)
}

private fun RawProperty.parseProperties(enums: List<Type.Enum>) = map { (name, configuration) ->
  val propertyType = configuration.type.parsePropertyType(enums)
  val optionalPlatforms = configuration.optional?.parsePlatforms().orEmpty()
  Property(name, propertyType, configuration.description, optionalPlatforms)
}

private fun YamlScalar.parsePropertyType(enums: List<Type.Enum>) = when (content) {
  "boolean" -> Type.Boolean
  "number" -> Type.Number
  "text" -> Type.Text
  else -> {
    val enum = enums.singleOrNull { enum -> enum.name == content }
    if (enum == null) {
      throw YamlException("Value '$content' must be one of boolean, number, text, or a predefined enum.", path)
    }
    enum
  }
}

private fun YamlNode.parsePlatforms() = when (this) {
  is YamlScalar -> parsePlatforms()
  is YamlList -> parsePlatforms()
  else -> throw YamlException("Expected element to be a scalar or a list but is ${this::class.simpleName}", path)
}

private fun YamlScalar.parsePlatforms() = when (content.toBooleanStrictOrNull()) {
  true -> Platform.entries.toSet()
  false -> emptySet()
  null -> throw YamlException("Value '$content' is not a valid boolean.", path)
}

private fun YamlList.parsePlatforms() = items.mapTo(mutableSetOf()) { item ->
  when (val content = item.yamlScalar.content) {
    "android" -> Platform.Android
    "ios" -> Platform.Ios
    "web" -> Platform.Web
    else -> throw YamlException("Value '$content' must be one of android, ios, or web.", path)
  }
}

@Serializable
private data class InputDefinition(
  val events: Map<String, Map<String, YamlNode>?> = emptyMap(),
  val enums: Map<String, Set<String>?> = emptyMap(),
)

@Serializable
private data class EventPropertyConfiguration(
  val type: YamlScalar,
  val optional: YamlNode? = null,
  val description: String? = null,
)

private typealias RawProperty = Map<String, EventPropertyConfiguration>
