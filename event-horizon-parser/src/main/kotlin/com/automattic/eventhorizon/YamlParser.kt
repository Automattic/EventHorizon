package com.automattic.eventhorizon

import arrow.core.toNonEmptySetOrThrow
import com.automattic.eventhorizon.CaseString.Companion.toCaseString
import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlException
import com.charleskorn.kaml.YamlList
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.YamlNode
import com.charleskorn.kaml.YamlNull
import com.charleskorn.kaml.YamlScalar
import com.charleskorn.kaml.YamlTaggedNode
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.yamlScalar
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlinx.serialization.Serializable

public class YamlParser {
  private val yaml = Yaml.default

  public fun parseSchema(file: Path): Result<Schema> = runCatching {
    val rawSchema = yaml.decodeFromStream<RawSchema>(file.inputStream())
    val version = requireNotNull(rawSchema.schemaVersion.toULongOrNull()) {
      "Value for 'schemaVersion' must be a number between 1 and ${ULong.MAX_VALUE}. Found: '${rawSchema.schemaVersion}'"
    }
    val platforms = rawSchema.platforms.mapTo(mutableSetOf(), ::Platform)
    val events = rawSchema.parseEvents(platforms)

    Schema.create(version, platforms, events)
  }.recoverCatching(::emptySchemaOrRethrow)

  private fun RawSchema.parseEvents(availablePlatforms: Set<Platform>): Events {
    val enums = parseEnums()
    val events = events.map { (name, mappings) ->
      val metadata = mappings?.parseMetadata()
      val description = metadata?.description
      val excludedPlatforms = metadata?.excludedPlatforms?.mapTo(mutableSetOf(), ::Platform).orEmpty()
      val properties = mappings?.parseProperties(enums, availablePlatforms).orEmpty()

      Event(name.toCaseString(), description, excludedPlatforms, properties)
    }
    return Events(events)
  }

  private fun RawSchema.parseEnums(): Set<PropertyType.Enum> {
    return enums.mapTo(mutableSetOf()) { (name, values) ->
      PropertyType.Enum(name.toCaseString(), values.orEmpty().toNonEmptySetOrThrow())
    }
  }

  private fun YamlStringMap.parseMetadata(): EventMetadata? {
    return get(MetadataKey)?.let { rawMetadata -> yaml.decodeFromYamlNode(rawMetadata) }
  }

  private fun YamlStringMap.parseProperties(availableEnums: Set<PropertyType.Enum>, availablePlatforms: Set<Platform>): List<Property> {
    return minus(MetadataKey).map { (name, rawConfiguration) ->
      val configuration = yaml.decodeFromYamlNode<EventPropertyConfiguration>(rawConfiguration)
      val type = configuration.propertyType(availableEnums)
      val description = configuration.description
      val optionalPlatforms = configuration.parseOptionalPlatforms(availablePlatforms)

      Property(name, type, description, optionalPlatforms)
    }
  }

  private fun EventPropertyConfiguration.propertyType(availableEnums: Set<PropertyType.Enum>): PropertyType {
    return when (val typeText = type.content) {
      "text" -> PropertyType.Text
      "number" -> PropertyType.Number
      "boolean" -> PropertyType.Boolean
      else -> availableEnums.find { enum -> enum.name.rawValue == typeText } ?: run {
        throw YamlException(
          "Value '$typeText' must be one of 'boolean', 'number', 'text', or a predefined enum.",
          type.path,
        )
      }
    }
  }

  private fun EventPropertyConfiguration.parseOptionalPlatforms(availablePlatforms: Set<Platform>): Set<Platform> {
    return when (optional) {
      is YamlScalar -> if (optional.toBoolean()) {
        availablePlatforms
      } else {
        emptySet()
      }
      is YamlList -> optional.items.mapTo(mutableSetOf()) { item -> Platform(item.yamlScalar.content) }
      null -> emptySet()
      is YamlNull, is YamlMap, is YamlTaggedNode -> {
        throw YamlException(
          "Expected element to be YamlScalar or YamlList but is ${optional::class.simpleName}",
          optional.path,
        )
      }
    }
  }

  private fun emptySchemaOrRethrow(throwable: Throwable): Schema {
    if (throwable !is EmptyYamlDocumentException) {
      throw throwable
    }
    return Schema.Empty
  }
}

@Serializable
private data class RawSchema(
  val schemaVersion: String,
  val platforms: Set<String> = emptySet(),
  val events: Map<String, Map<String, YamlMap>?> = emptyMap(),
  val enums: Map<String, Set<String>?> = emptyMap(),
)

@Serializable
private data class EventMetadata(
  val description: String? = null,
  val excludedPlatforms: Set<String> = emptySet(),
)

@Serializable
private data class EventPropertyConfiguration(
  val type: YamlScalar,
  val optional: YamlNode? = null,
  val description: String? = null,
)

private typealias YamlStringMap = Map<String, YamlNode>

private const val MetadataKey = "_metadata"
