package com.automattic.eventhorizon

import arrow.core.EitherNel
import arrow.core.Nel
import arrow.core.mapOrAccumulate
import arrow.core.nonEmptyListOf
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import arrow.core.recover
import com.charleskorn.kaml.EmptyYamlDocumentException
import com.charleskorn.kaml.InvalidPropertyValueException
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
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlinx.serialization.Serializable

public class YamlParser {
  private val yaml = Yaml.default

  public fun parseSchema(file: Path): EitherNel<Problem, Schema> = either {
    val rawSchema = decodeRawSchema(file.inputStream())
    val version = parseSchemaVersion(rawSchema.schemaVersion)
    val platforms = rawSchema.platforms.mapTo(mutableSetOf(), ::Platform)
    val events = parseEvents(rawSchema, platforms)

    Schema(version, platforms, events).mapLeft { nonEmptyListOf(it) }.bind()
  }.recover { problems -> recoverEmptySchema(problems) }

  private fun Raise<Nel<Problem>>.parseSchemaVersion(rawVersion: YamlScalar): ULong {
    val content = rawVersion.content
    return ensureNotNull(content.toULongOrNull()) {
      val exception = InvalidPropertyValueException(
        propertyName = "schemaVersion",
        reason = "Value '$content' is not a valid unsigned long value.",
        path = rawVersion.path,
      )
      raise(nonEmptyListOf(GenericProblem(exception)))
    }
  }

  private fun Raise<Nel<Problem>>.parseEvents(schema: RawSchema, availablePlatforms: Set<Platform>): List<Event> {
    val enums = parseEnums(schema)
    val events = schema.events.orEmpty()
    val metadataMap = events.mapValues { (_, mappings) ->
      mappings?.get(MetadataKey)?.let { decodeEventMetadata(it) }
    }
    val propertiesMap = events.mapValues { (_, mappings) ->
      mappings?.minus(MetadataKey)?.let { parseProperties(it, enums, availablePlatforms) }
    }
    return events.toList().mapOrAccumulate { (name, _) ->
      val groupKey = metadataMap[name]?.group ?: Group.empty.key.rawValue
      val properties = propertiesMap[name].orEmpty()
      val description = metadataMap[name]?.description
      val excludedPlatforms = metadataMap[name]?.excludedPlatforms?.mapTo(mutableSetOf(), ::Platform).orEmpty()
      Event(name, groupKey, properties, description, excludedPlatforms).bind()
    }.bind()
  }

  private fun Raise<Nel<Problem>>.parseEnums(schema: RawSchema): Set<PropertyType.Enum> {
    return schema.enums.orEmpty().toList()
      .mapOrAccumulate { (name, values) -> PropertyType.Enum(name, values.orEmpty()).bind() }
      .map { it.toSet() }
      .bind()
  }

  private fun Raise<Nel<Problem>>.parseProperties(
    rawProperties: YamlStringMap,
    availableEnums: Set<PropertyType.Enum>,
    availablePlatforms: Set<Platform>,
  ): List<Property> {
    return rawProperties.toList()
      .mapOrAccumulate { (name, rawConfiguration) ->
        val configuration = decodePropertyConfiguration(rawConfiguration)
        val type = parsePropertyType(configuration, availableEnums)
        val optionalPlatforms = parseOptionalPlatforms(configuration, availablePlatforms)

        Property(name, type, configuration.description, optionalPlatforms).bind()
      }
      .bind()
  }

  private fun Raise<Problem>.parsePropertyType(configuration: PropertyConfiguration, availableEnums: Set<PropertyType.Enum>): PropertyType {
    return when (val typeText = configuration.type.content) {
      "text" -> PropertyType.Text
      "number" -> PropertyType.Number
      "boolean" -> PropertyType.Boolean
      else -> availableEnums.find { enum -> enum.name.rawValue == typeText } ?: run {
        val exception = YamlException(
          "Value '$typeText' must be one of 'boolean', 'number', 'text', or a predefined enum.",
          configuration.type.path,
        )
        raise(GenericProblem(exception))
      }
    }
  }

  private fun Raise<Problem>.parseOptionalPlatforms(
    configuration: PropertyConfiguration,
    availablePlatforms: Set<Platform>,
  ): Set<Platform> {
    return when (val optional = configuration.optional) {
      is YamlScalar -> if (optional.toBoolean()) {
        availablePlatforms
      } else {
        emptySet()
      }

      is YamlList -> optional.items.mapTo(mutableSetOf()) { item -> Platform(item.yamlScalar.content) }
      null -> emptySet()
      is YamlNull, is YamlMap, is YamlTaggedNode -> {
        val exception = YamlException(
          "Expected element to be YamlScalar or YamlList but is ${optional::class.simpleName}",
          optional.path,
        )
        raise(GenericProblem(exception))
      }
    }
  }

  private fun Raise<Nel<Problem>>.decodeRawSchema(stream: InputStream): RawSchema {
    return catch({ yaml.decodeFromStream(stream) }, ::raiseYamlProblem)
  }

  private fun Raise<Nel<Problem>>.decodeEventMetadata(node: YamlNode): EventMetadata {
    return catch({ yaml.decodeFromYamlNode(node) }, ::raiseYamlProblem)
  }

  private fun Raise<Problem>.decodePropertyConfiguration(node: YamlNode): PropertyConfiguration {
    return catch({ yaml.decodeFromYamlNode(node) }, ::raiseYamlProblem)
  }
}

@JvmName("raiseYamlNelProblem")
private fun Raise<Nel<Problem>>.raiseYamlProblem(error: Throwable): Nothing {
  if (error is YamlException) {
    raise(nonEmptyListOf(GenericProblem(error, Throwable::toString)))
  } else {
    throw error
  }
}

private fun Raise<Problem>.raiseYamlProblem(error: Throwable): Nothing {
  if (error is YamlException) {
    raise(GenericProblem(error, Throwable::toString))
  } else {
    throw error
  }
}

private fun Raise<Nel<Problem>>.recoverEmptySchema(problems: Nel<Problem>): Schema {
  val singleProblem = problems.singleOrNull()
  ensure(singleProblem is GenericProblem && singleProblem.error is EmptyYamlDocumentException) { problems }
  return Schema.empty
}

@Serializable
private data class RawSchema(
  val schemaVersion: YamlScalar,
  val platforms: Set<String> = emptySet(),
  val events: Map<String, Map<String, YamlMap>?>? = emptyMap(),
  val enums: Map<String, Set<String>?>? = emptyMap(),
  val groups: Map<String, GroupConfiguration?>? = emptyMap(),
)

@Serializable
private data class EventMetadata(
  val description: String? = null,
  val excludedPlatforms: Set<String> = emptySet(),
  val group: String? = null,
)

@Serializable
private data class PropertyConfiguration(
  val type: YamlScalar,
  val optional: YamlNode? = null,
  val description: String? = null,
)

@Serializable
private data class GroupConfiguration(
  val name: String? = null,
  val description: String? = null,
)

private typealias YamlStringMap = Map<String, YamlNode>

private const val MetadataKey = "_metadata"
