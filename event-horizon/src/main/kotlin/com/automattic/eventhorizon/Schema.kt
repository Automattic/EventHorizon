package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.automattic.eventhorizon.utils.ensureNoDuplicatesBy

@ConsistentCopyVisibility
public data class Schema private constructor(
  val version: ULong,
  val platforms: Set<Platform>,
  val events: List<Event>,
) {
  public fun platformEvents(platform: Platform): List<Event> {
    return events.filter { event -> platform !in event.excludedPlatforms }
  }

  public fun platformEnums(platform: Platform): List<PropertyType.Enum> {
    return platformEvents(platform)
      .flatMap(Event::properties)
      .map(Property::type)
      .filterIsInstance<PropertyType.Enum>()
      .distinctBy { enum -> enum.name.rawValue }
      .sortedWith(EnumPropertyNameComparator)
  }

  public companion object {
    public val Empty: Schema = Schema(
      version = 0u,
      platforms = emptySet(),
      events = emptyList(),
    )

    internal val SupportedVersions = listOf<ULong>(
      1u,
    )

    public operator fun invoke(version: ULong, platforms: Set<Platform>, events: List<Event>): Either<SchemaProblem, Schema> = either {
      ensureSchemaVersion(version)
      ensureUniqueEvents(events)
      ensureEventPlatforms(events, platforms)
      ensurePropertyPlatforms(events, platforms)
      ensureConsistentEnums(events)

      Schema(version, platforms, events)
    }
  }
}

public sealed interface SchemaProblem : Problem {
  public data class InvalidSchemaVersion(val version: ULong) : SchemaProblem {
    override fun print(): String {
      return "Unsupported schema version '$version'. Must be one of: ${Schema.SupportedVersions}"
    }
  }

  public data class DuplicateEvents(val duplicates: Map<String, Int>) : SchemaProblem {
    override fun print(): String {
      return "Found duplicate events: $duplicates"
    }
  }

  public data class UnknownEventPlatforms(
    val unknownPlatforms: Map<String, List<String>>,
    val availablePlatforms: List<String>,
  ) : SchemaProblem {
    override fun print(): String {
      return buildString {
        append("Found events with platforms undeclared in schema:\n")
        unknownPlatforms.forEach { (event, platforms) ->
          append(" - ")
          append(event)
          append(": ")
          append(platforms)
          append('\n')
        }
        append("Available platforms:\n")
        append(availablePlatforms.joinToString(separator = "\n") { platform -> " - $platform" })
      }
    }
  }

  public data class UnknownPropertyPlatforms(
    val unknownPlatforms: Map<String, Map<String, List<String>>>,
    val availablePlatforms: List<String>,
  ) : SchemaProblem {
    override fun print(): String {
      return buildString {
        append("Found event properties with platforms undeclared in schema:\n")
        unknownPlatforms.forEach { (event, propertyPlatforms) ->
          append(" - ")
          append(event)
          append(":\n")
          propertyPlatforms.forEach { (property, platforms) ->
            append("   - ")
            append(property)
            append(": ")
            append(platforms)
            append('\n')
          }
        }
        append("Available platforms:\n")
        append(availablePlatforms.joinToString(separator = "\n") { platform -> " - $platform" })
      }
    }
  }

  public data class InconsistentEnumValues(val inconsistentValues: Map<String, List<List<String>>>) : SchemaProblem {
    override fun print(): String {
      return buildString {
        append("Following enums have inconsistent values:\n")
        inconsistentValues.forEach { (enum, differentValues) ->
          append(" - ")
          append(enum)
          append(":\n")
          append(differentValues.joinToString(separator = "\n") { values -> "   - $values" })
        }
      }
    }
  }
}

private fun Raise<SchemaProblem>.ensureSchemaVersion(version: ULong) {
  ensure(version in Schema.SupportedVersions) { raise(SchemaProblem.InvalidSchemaVersion(version)) }
}

private fun Raise<SchemaProblem>.ensureUniqueEvents(events: List<Event>) {
  ensureNoDuplicatesBy(events, Event::uniqueKey) { duplicates ->
    SchemaProblem.DuplicateEvents(duplicates)
  }
}

private fun Raise<SchemaProblem>.ensureEventPlatforms(events: List<Event>, availablePlatforms: Set<Platform>) {
  val unknownPlatforms = events.findUnknownPlatforms(availablePlatforms)
  if (unknownPlatforms.isNotEmpty()) {
    raise(SchemaProblem.UnknownEventPlatforms(unknownPlatforms, availablePlatforms.map(Platform::value)))
  }
}

private fun List<Event>.findUnknownPlatforms(availablePlatforms: Set<Platform>) = buildMap {
  this@findUnknownPlatforms.forEach { event ->
    val invalidPlatforms = event.excludedPlatforms.filter { platform -> platform !in availablePlatforms }
    if (invalidPlatforms.isNotEmpty()) {
      put(event.name.rawValue, invalidPlatforms.map(Platform::value))
    }
  }
}

private fun Raise<SchemaProblem>.ensurePropertyPlatforms(events: List<Event>, availablePlatforms: Set<Platform>) {
  val unknownPlatforms = events.findUnknownPropertyPlatforms(availablePlatforms)
  if (unknownPlatforms.isNotEmpty()) {
    raise(SchemaProblem.UnknownPropertyPlatforms(unknownPlatforms, availablePlatforms.map(Platform::value)))
  }
}

private fun List<Event>.findUnknownPropertyPlatforms(availablePlatforms: Set<Platform>) = buildMap {
  this@findUnknownPropertyPlatforms.forEach { event ->
    val unknownPlatforms = event.findUnknownPropertyPlatforms(availablePlatforms)
    if (unknownPlatforms.isNotEmpty()) {
      put(event.name.rawValue, unknownPlatforms)
    }
  }
}

private fun Event.findUnknownPropertyPlatforms(availablePlatforms: Set<Platform>) = buildMap {
  properties.forEach { property ->
    val invalidPlatforms = property.optionalPlatforms.filter { platform -> platform !in availablePlatforms }
    if (invalidPlatforms.isNotEmpty()) {
      put(property.name.rawValue, invalidPlatforms.map(Platform::value))
    }
  }
}

private fun Raise<SchemaProblem>.ensureConsistentEnums(events: List<Event>) {
  val inconsistentEnums = events
    .flatMap(Event::properties)
    .map(Property::type)
    .filterIsInstance<PropertyType.Enum>()
    .groupBy { type -> type.name.rawValue }
    .mapValues { (_, values) -> values.map(PropertyType.Enum::rawValues).distinct() }
    .filter { (_, groupedValues) -> groupedValues.size != 1 }

  if (inconsistentEnums.isNotEmpty()) {
    raise(SchemaProblem.InconsistentEnumValues(inconsistentEnums))
  }
}

private val Event.uniqueKey get() = name.rawValue

private val PropertyType.Enum.rawValues get() = values.map(CaseString::rawValue)

private object EnumPropertyNameComparator : Comparator<PropertyType.Enum> {
  override fun compare(o1: PropertyType.Enum, o2: PropertyType.Enum): Int {
    return o1.name.rawValue.compareTo(o2.name.rawValue)
  }
}
