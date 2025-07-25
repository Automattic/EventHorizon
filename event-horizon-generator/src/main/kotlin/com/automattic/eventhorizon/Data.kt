package com.automattic.eventhorizon

import com.automattic.eventhorizon.Property.Type
import java.util.function.IntFunction

@ConsistentCopyVisibility
public data class EventHorizonSchema private constructor(
  val schemaVersion: ULong,
  val events: Events,
) {
  public companion object {
    public val Empty: EventHorizonSchema = EventHorizonSchema(
      schemaVersion = 0u,
      events = Events(),
    )

    public fun create(schemaVersion: ULong, events: Events): EventHorizonSchema {
      require(schemaVersion > 0u) { "Schema version must be a positive number. Is: $schemaVersion" }
      return EventHorizonSchema(
        schemaVersion = schemaVersion,
        events = events,
      )
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
  public val name: String,
  public val description: String?,
  public val properties: List<Property>,
) {
  init {
    requireNoDuplicates(properties.map(Property::name)) { duplicates ->
      "Found duplicate properties for event '$name': $duplicates"
    }
  }

  public constructor(
    name: String,
    vararg properties: Property,
    description: String? = null,
  ) : this(name, description, properties.toList())
}

public data class Property(
  val name: String,
  val type: Type,
  val description: String?,
  val optionalPlatforms: Set<Platform>,
) {
  public fun isOptional(platform: Platform): Boolean = platform in optionalPlatforms

  public constructor(
    name: String,
    type: Type = Type.Text,
    description: String? = null,
    optAndroid: Boolean = false,
    optIos: Boolean = false,
    optWeb: Boolean = false,
  ) : this(
    name,
    type,
    description,
    buildSet {
      if (optAndroid) {
        add(Platform.Android)
      }
      if (optIos) {
        add(Platform.Ios)
      }
      if (optWeb) {
        add(Platform.Web)
      }
    },
  )

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

      public constructor(
        name: String,
        value: String,
        vararg values: String,
      ) : this(name, setOf(value) + values.toSet())
    }
  }
}

public enum class Platform {
  Android,
  Ios,
  Web,
}

private fun <T> requireNoDuplicates(collection: Collection<T>, message: (Map<T, Int>) -> String) {
  require(collection.distinct().size == collection.size) {
    val duplicates = collection.groupingBy { it }.eachCount().filter { (_, count) -> count > 1 }
    message(duplicates)
  }
}
