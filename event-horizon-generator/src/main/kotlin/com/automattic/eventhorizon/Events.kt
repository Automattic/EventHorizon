package com.automattic.eventhorizon

import java.util.function.IntFunction

public class Events(
  private val value: List<Event>,
) : List<Event> by value {
  init {
    requireNoDuplicates(value.map(Event::name)) { duplicates ->
      "Found duplicate events: $duplicates"
    }
  }

  public val distinctEnums: List<PropertyType.Enum>
    get() = flatMap(Event::properties)
      .map(Property::type)
      .filterIsInstance<PropertyType.Enum>()
      .distinctBy(PropertyType.Enum::name)
      .sortedBy(PropertyType.Enum::name)

  @Deprecated("Deprecated in Kotlin", level = DeprecationLevel.HIDDEN)
  override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> {
    @Suppress("DEPRECATION")
    return super.toArray(generator)
  }

  override fun equals(other: Any?): Boolean = other === this || (other is Events && other.value == this.value)

  override fun hashCode(): Int = value.hashCode()

  override fun toString(): String = "Events($value)"
}
