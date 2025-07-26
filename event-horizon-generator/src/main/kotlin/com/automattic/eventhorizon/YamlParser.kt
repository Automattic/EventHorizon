package com.automattic.eventhorizon

import arrow.core.toNonEmptySetOrThrow
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.yamlScalar
import java.nio.file.Path
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.orEmpty
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream
import kotlinx.serialization.Serializable

public class YamlParser {
  private val yaml = Yaml.default

  public fun parseSchema(file: Path): Result<Schema> = runCatching {
    if (file.fileSize() != 0L) {
      parseFile(file)
    } else {
      Schema.Empty
    }
  }

  private fun parseFile(file: Path): Schema {
    val definition = yaml.decodeFromStream<InputDefinition>(file.inputStream())
    val enums = definition.enums.map { (name, values) ->
      PropertyType.Enum(name, values.orEmpty().toNonEmptySetOrThrow())
    }
    val platforms = definition.platforms.mapTo(mutableSetOf(), ::Platform)
    val events = definition.parseEvents(enums, platforms)
    return Schema.create(
      version = requireNotNull(definition.version.toULongOrNull()) {
        "Schema version must be a positive number. Is: ${definition.version}"
      },
      platforms = platforms,
      events = Events(events),
    )
  }

  private fun InputDefinition.parseEvents(enums: List<PropertyType.Enum>, platforms: Set<Platform>) =
    events.map { (eventName, rawProperties) ->
      val documentation = rawProperties?.parseDocumentation()
      val optOutPlatforms = rawProperties?.parsePlatforms().orEmpty()
      val properties = rawProperties
        ?.minus(setOf(DocumentationNode, OptOutPlatformsNode))
        ?.mapValues { (_, rawProperty) -> yaml.decodeFromYamlNode<EventPropertyConfiguration>(rawProperty) }
        ?.parseProperties(enums, platforms)
        .orEmpty()
      Event(
        name = eventName,
        documentation = documentation,
        properties = properties,
        excludedPlatforms = optOutPlatforms,
      )
    }

  private fun Map<String, YamlNode>.parseDocumentation() = when (val yamlDocumentation = get(DocumentationNode)) {
    is YamlScalar -> yamlDocumentation.content
    is YamlNull, null -> null
    else -> throw YamlException("'$DocumentationNode' cannot be used as a property name", yamlDocumentation.path)
  }

  private fun Map<String, YamlNode>.parsePlatforms() = when (val yamlPlatforms = get(OptOutPlatformsNode)) {
    is YamlList -> yamlPlatforms.items.mapTo(mutableSetOf()) { item ->
      Platform(item.yamlScalar.content)
    }
    is YamlNull, null -> emptySet()
    else -> throw YamlException("'$OptOutPlatformsNode' cannot be used as a property name", yamlPlatforms.path)
  }

  private fun RawProperty.parseProperties(enums: List<PropertyType.Enum>, availablePlatforms: Set<Platform>) =
    map { (name, configuration) ->
      val propertyType = configuration.type.parsePropertyType(enums)
      val optionalPlatforms = configuration.optional?.parsePlatforms(availablePlatforms).orEmpty()
      Property(name, propertyType, configuration.documentation, optionalPlatforms)
    }

  private fun YamlScalar.parsePropertyType(enums: List<PropertyType.Enum>) = when (content) {
    "boolean" -> PropertyType.Boolean
    "number" -> PropertyType.Number
    "text" -> PropertyType.Text
    else -> {
      val enum = enums.singleOrNull { enum -> enum.name == content }
      if (enum == null) {
        throw YamlException("Value '$content' must be one of boolean, number, text, or a predefined enum.", path)
      }
      enum
    }
  }

  private fun YamlNode.parsePlatforms(availablePlatforms: Set<Platform>) = when (this) {
    is YamlScalar -> parsePlatforms(availablePlatforms)
    is YamlList -> parsePlatforms()
    else -> throw YamlException("Expected element to be a scalar or a list but is ${this::class.simpleName}", path)
  }

  private fun YamlScalar.parsePlatforms(availablePlatforms: Set<Platform>) = when (content.toBooleanStrictOrNull()) {
    true -> availablePlatforms
    false -> emptySet()
    null -> throw YamlException("Value '$content' is not a valid boolean.", path)
  }

  private fun YamlList.parsePlatforms() = items.mapTo(mutableSetOf()) { item ->
    Platform(item.yamlScalar.content)
  }
}

@Serializable
private data class InputDefinition(
  val version: String,
  val platforms: Set<String> = emptySet(),
  val events: Map<String, Map<String, YamlNode>?> = emptyMap(),
  val enums: Map<String, Set<String>?> = emptyMap(),
)

@Serializable
private data class EventPropertyConfiguration(
  val type: YamlScalar,
  val optional: YamlNode? = null,
  val documentation: String? = null,
)

private const val DocumentationNode = "documentation"
private const val OptOutPlatformsNode = "optOut"

private typealias RawProperty = Map<String, EventPropertyConfiguration>
