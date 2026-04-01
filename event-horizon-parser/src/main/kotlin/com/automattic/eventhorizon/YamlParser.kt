package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.nonEmptyListOf
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLParser
import java.nio.file.Path
import kotlin.io.path.readText

public class YamlParser {
  private val mapper = YAMLMapper.builder()
    .enable(YAMLParser.Feature.PARSE_BOOLEAN_LIKE_WORDS_AS_STRINGS)
    .enable(DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY)
    .build()

  public fun parseSchema(file: Path): Either<Problem, Schema> = either {
    val content = file.readText().trim()
    if (content.isEmpty()) {
      Schema.empty
    } else {
      val jsonRoot = mapper.readTree(content)
      if (jsonRoot.isEmpty) {
        raise(SimpleProblem("Invalid schema content:\n$content"))
      } else {
        val root = SafeNode(jsonRoot)
        val children = root.ensureChildren("schemaVersion", "platforms", "groups", "enums", "events", "reservedProperties").bind()
        val version = children.ensureValue("schemaVersion").bind().ensureULong().bind()
        val platforms = children["platforms"]?.let { parsePlatforms(it) }.orEmpty()
        val groups = children["groups"]?.let { parseGroups(it) }.orEmpty()
        val enums = children["enums"]?.let { parseEnums(it) }.orEmpty()
        val events = children["events"]?.let { parseEvents(it, platforms, enums) }.orEmpty()
        val reservedProperties = children["reservedProperties"]?.let { parseReservedProperties(it) }.orEmpty()

        Schema(version, platforms, groups, events, reservedProperties).bind()
      }
    }
  }

  private fun Raise<Problem>.parsePlatforms(node: SafeNode): Set<Platform> {
    val node = node.ensureArray().bind()
    return node
      .mapTo(HashSet(node.size)) { node -> Platform(node.ensureText().bind()) }
      .toSortedSet(compareBy(Platform::value))
  }

  private fun Raise<Problem>.parseGroups(node: SafeNode): List<Group> {
    val node = node.ensureObject().bind()
    return node.mapTo(ArrayList(node.size)) { (key, node) ->
      val children = node.ensureChildren("name", "description").bind()
      val name = children["name"]?.ensureText()?.bind()
      val description = children["description"]?.ensureText()?.bind()

      Group(key, name, description).bind()
    }
  }

  private fun Raise<Problem>.parseEnums(node: SafeNode): Set<PropertyType.Enum> {
    val node = node.ensureObject().bind()
    return node.mapTo(HashSet(node.size)) { (key, node) ->
      val children = node.ensureArray().bind()
      val values = children.mapTo(HashSet(children.size)) { node -> node.ensureText().bind() }

      PropertyType.Enum(key, values).bind()
    }
  }

  private fun Raise<Problem>.parseEvents(node: SafeNode, platforms: Set<Platform>, enums: Set<PropertyType.Enum>): List<Event> {
    val eventNodes = node.ensureObject().bind()
    val metadataMap = HashMap<String, EventMetadata>(eventNodes.size)
    val propertiesMap = HashMap<String, List<Property>>(eventNodes.size)

    for ((key, mappingsNode) in eventNodes) {
      val mappings = mappingsNode.ensureObject().bind()
      val metadata = mappings["_metadata"]?.let { parseEventMetadata(it) }
      if (metadata != null) {
        metadataMap[key] = metadata
      }
      val properties = parseEventProperties((mappings - "_metadata"), platforms, enums)
      propertiesMap[key] = properties
    }

    return eventNodes.mapTo(ArrayList(eventNodes.size)) { (name, _) ->
      val groupKey = metadataMap[name]?.group ?: Group.empty.key.rawValue
      val properties = propertiesMap[name].orEmpty()
      val description = metadataMap[name]?.description
      val includedPlatforms = metadataMap[name]?.includedPlatforms
      val excludedPlatforms = if (includedPlatforms != null) {
        platforms - includedPlatforms
      } else {
        emptySet()
      }

      Event(name, groupKey, properties, description, excludedPlatforms).bind()
    }
  }

  private fun Raise<Problem>.parseEventMetadata(node: SafeNode): EventMetadata {
    val children = node.ensureChildren("description", "group", "includedPlatforms").bind()
    val description = children["description"]?.ensureText()?.bind()
    val group = children["group"]?.ensureText()?.bind()
    val platforms = children["includedPlatforms"]?.let { parsePlatforms(it) }

    return EventMetadata(description, group, platforms)
  }

  private fun Raise<Problem>.parseEventProperties(
    nodes: Map<String, SafeNode>,
    availablePlatforms: Set<Platform>,
    availableEnums: Set<PropertyType.Enum>,
  ): List<Property> {
    return nodes.mapTo(ArrayList(nodes.size)) { (name, node) ->
      val children = node.ensureChildren("type", "description", "optional").bind()
      val typeNode = children.ensureValue("type").bind()
      val type = when (val typeText = typeNode.ensureText().bind()) {
        "text" -> PropertyType.Text

        "int" -> PropertyType.NumberInt

        "float" -> PropertyType.NumberFloat

        "boolean" -> PropertyType.Boolean

        else -> ensureNotNull(availableEnums.find { enum -> enum.name.rawValue == typeText }) {
          SimpleProblem(
            "Invalid value at path '${typeNode.path}'. Expected one of 'boolean', 'int', 'float', 'text', or a predefined enum, but was '$typeText'.",
          )
        }
      }
      val description = children["description"]?.ensureText()?.bind()
      val optionalNode = children["optional"]
      val optionalPlatforms = when {
        optionalNode == null -> emptySet()

        optionalNode.isBoolean -> {
          if (optionalNode.ensureBoolean().bind()) {
            availablePlatforms
          } else {
            emptySet()
          }
        }

        optionalNode.isArray -> {
          parsePlatforms(optionalNode)
        }

        else -> {
          raise(SimpleProblem("Invalid value at path '${optionalNode.path}'. Expected a boolean or an array of platforms."))
        }
      }

      Property(name, type, description, optionalPlatforms).bind()
    }
  }

  private fun Raise<Problem>.parseReservedProperties(node: SafeNode): Set<CaseString> {
    val node = node.ensureArray().bind()
    return node.flatMapTo(HashSet(node.size)) { node ->
      val text = node.ensureText().bind()
      if (text.startsWith("predefined:")) {
        val predefined = text.substringAfter("predefined:")
        ensureNotNull(reserverPropertiesMap[predefined]) {
          SimpleProblem("Invalid predefined reserved properties '$predefined'. Expected one of $knownReservedProperties.")
        }
      } else {
        setOf(
          CaseString(node.ensureText().bind())
            .mapLeft { value -> SimpleProblem("Invalid reserved property value '$value'. ${Case.supportedConventionsMessage}.") }
            .bind(),
        )
      }
    }
  }

  internal companion object {
    private val reserverPropertiesMap = mapOf(
      "tracks" to Schema.tracksReservedProperties,
    )

    val knownReservedProperties get() = reserverPropertiesMap.keys
  }
}

private class SafeNode(
  private val node: JsonNode,
  private val pathSegments: NonEmptyList<String>,
) {
  val path get() = pathSegments.joinToString(separator = ".")

  constructor(node: JsonNode) : this(node, pathSegments = nonEmptyListOf("$"))

  val isBoolean get() = node.isBoolean

  val isArray get() = node.isArray

  fun ensureText() = either<Problem, String> {
    ensure(node.isValueNode) {
      SimpleProblem("Invalid value at path '$path'. Expected a scalar.")
    }
    node.asText()
  }

  fun ensureULong() = either {
    val value = ensureText().bind()
    ensureNotNull(value.toULongOrNull()) {
      raise(SimpleProblem("Invalid value at path '$path'. Expected an unsigned long."))
    }
  }

  fun ensureBoolean() = either<Problem, Boolean> {
    ensure(node.isBoolean) {
      SimpleProblem("Invalid value at path '$path'. Expected a boolean.")
    }
    node.asBoolean()
  }

  fun ensureArray() = either<Problem, List<SafeNode>> {
    ensure(node.isArray || node.isNull) {
      SimpleProblem("Invalid value at path '$path'. Expected an array.")
    }
    node.toList().map { node -> SafeNode(node, pathSegments) }
  }

  fun ensureObject() = either<Problem, Map<String, SafeNode>> {
    ensure(node.isObject || node.isNull) {
      SimpleProblem("Invalid value at path '$path'. Expected an object.")
    }
    node.properties().associate { (key, node) ->
      key to SafeNode(node, pathSegments + key)
    }
  }

  fun ensureChildren(name: String, vararg names: String) = either {
    val expectedNames = setOf(name, *names)
    val properties = ensureObject().bind()
    val unexpectedNames = properties.keys - expectedNames
    ensure(unexpectedNames.isEmpty()) {
      SimpleProblem("Invalid value at path '$path'. Unexpected keys: $unexpectedNames.")
    }
    val value = buildMap(expectedNames.size) {
      for (name in expectedNames) {
        val node = properties[name]
        if (node != null) {
          put(name, node)
        }
      }
    }
    SafeMap(value, path)
  }
}

private class SafeMap(
  private val value: Map<String, SafeNode>,
  private val rootPath: String,
) {
  operator fun get(key: String) = value[key]

  fun ensureValue(key: String) = either {
    ensureNotNull(get(key)) {
      SimpleProblem("Invalid value at path '$rootPath': missing required key '$key'.")
    }
  }
}

private class EventMetadata(
  val description: String?,
  val group: String?,
  val includedPlatforms: Set<Platform>?,
)
