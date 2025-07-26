package com.automattic.eventhorizon

import com.automattic.eventhorizon.Property.Type
import java.util.function.IntFunction

@ConsistentCopyVisibility
public data class Schema private constructor(
  val schemaVersion: ULong,
  val availablePlatforms: Set<Platform>,
  val events: Events,
) {
  public companion object {
    public val Empty: Schema = Schema(
      schemaVersion = 0u,
      availablePlatforms = emptySet(),
      events = Events(),
    )

    public fun create(schemaVersion: ULong, availablePlatforms: Set<Platform>, events: Events): Schema {
      require(schemaVersion > 0u) { "Schema version must be a positive number. Is: $schemaVersion" }
      requirePredeclaredEventPlatforms(events, availablePlatforms)
      requirePredeclaredPropertyPlatforms(events, availablePlatforms)
      return Schema(
        schemaVersion = schemaVersion,
        availablePlatforms = availablePlatforms,
        events = events,
      )
    }

    private fun requirePredeclaredEventPlatforms(events: Events, availablePlatforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidEventPlatforms(availablePlatforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Schema must declare platforms for optional events. Available platforms:\n")
          val platforms = availablePlatforms.joinToString(separator = "\n") { platform ->
            " - ${platform.value}"
          }
          append(platforms)

          append("\nIssues found with the following events:\n")
          val eventIssues = invalidPlatforms.joinToString(separator = "\n") { (eventName, platformNames) ->
            " - $eventName: $platformNames"
          }
          append(eventIssues)
        }
      }
    }

    private fun requirePredeclaredPropertyPlatforms(events: Events, availablePlatforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidPropertyPlatforms(availablePlatforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Schema must declare platforms for optional properties. Available platforms:\n")
          val platforms = availablePlatforms.joinToString(separator = "\n") { platform ->
            " - ${platform.value}"
          }
          append(platforms)

          append("\nIssues found with the following events and properties:\n")
          val eventIssues = invalidPlatforms.joinToString(separator = "\n") { (eventName, propertyNames) ->
            val propertyIssues = propertyNames.joinToString(separator = "\n") { (propertyName, platformNames) ->
              "   - $propertyName: $platformNames"
            }
            " - $eventName:\n$propertyIssues"
          }
          append(eventIssues)
        }
      }
    }

    private fun Events.findInvalidEventPlatforms(availablePlatforms: Set<Platform>) = mapNotNull { event ->
      val invalidPlatforms = event.availablePlatforms.filter { platform ->
        platform !in availablePlatforms
      }
      if (invalidPlatforms.isNotEmpty()) {
        event.name to invalidPlatforms.map(Platform::value)
      } else {
        null
      }
    }

    private fun Events.findInvalidPropertyPlatforms(availablePlatforms: Set<Platform>) = mapNotNull { event ->
      val invalidProperties = event.properties.mapNotNull { property ->
        val invalidPlatforms = property.optionalPlatforms.filter { platform ->
          platform !in availablePlatforms
        }
        if (invalidPlatforms.isNotEmpty()) {
          property.name to invalidPlatforms.map(Platform::value)
        } else {
          null
        }
      }
      if (invalidProperties.isNotEmpty()) {
        event.name to invalidProperties
      } else {
        null
      }
    }
  }
}

public data class Events(
  private val events: List<Event>,
) : List<Event> by events {
  init {
    requireNoDuplicates(events.map(Event::name)) { duplicates ->
      "Found duplicate events: $duplicates"
    }
  }

  public constructor(vararg events: Event) : this(events.toList())

  public val distinctEnums: List<Type.Enum>
    get() = flatMap(Event::properties)
      .map(Property::type)
      .filterIsInstance<Type.Enum>()
      .distinctBy(Type.Enum::name)
      .sortedBy(Type.Enum::name)

  @Deprecated("Deprecated in Kotlin", level = DeprecationLevel.HIDDEN)
  override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> {
    @Suppress("DEPRECATION")
    return super.toArray(generator)
  }
}

public data class Event(
  val name: String,
  val documentation: String?,
  val properties: List<Property>,
  val availablePlatforms: Set<Platform>,
) {
  init {
    requireNoDuplicates(properties.map(Property::name)) { duplicates ->
      "Found duplicate properties for event '$name': $duplicates"
    }
  }

  public constructor(
    name: String,
    vararg properties: Property,
    documentation: String? = null,
    availablePlatforms: Set<String> = emptySet(),
  ) : this(
    name = name,
    documentation = documentation,
    properties = properties.toList(),
    availablePlatforms = availablePlatforms.mapTo(mutableSetOf(), ::Platform),
  )
}

public data class Property(
  val name: String,
  val type: Type,
  val documentation: String?,
  val optionalPlatforms: Set<Platform>,
) {
  public fun isOptional(platform: Platform): Boolean = platform in optionalPlatforms

  public companion object {
    public fun test(
      name: String,
      type: Type = Type.Text,
      documentation: String? = null,
      optionalPlatforms: Set<String> = emptySet(),
    ): Property = Property(
      name = name,
      type = type,
      documentation = documentation,
      optionalPlatforms = optionalPlatforms.mapTo(mutableSetOf(), ::Platform),
    )
  }

  public sealed interface Type {
    public data object Text : Type

    public data object Number : Type

    public data object Boolean : Type

    public data class Enum(
      val name: String,
      val values: Set<String>,
    ) : Type {
      init {
        require(values.isNotEmpty()) { "Enum property '$name' has no values" }
      }

      public companion object {
        public fun test(name: String, value: String, vararg values: String): Enum =
          Enum(name, setOf(value) + values.toSet())
      }
    }
  }
}

@JvmInline
public value class Platform(
  public val value: String,
) {
  public companion object {
    public val NoPlatform: Platform = Platform("")
  }
}

private fun <T> requireNoDuplicates(collection: Collection<T>, message: (Map<T, Int>) -> String) {
  require(collection.distinct().size == collection.size) {
    val duplicates = collection.groupingBy { it }.eachCount().filter { (_, count) -> count > 1 }
    message(duplicates)
  }
}
