package com.automattic.eventhorizon

import java.util.function.IntFunction

public class Events(
  private val entries: List<Event>,
) : List<Event> by entries {
  init {
    requireNoDuplicates(entries.map { event -> event.name.rawValue }) { duplicates ->
      "Found duplicate events: $duplicates"
    }
  }

  public val distinctEnums: List<PropertyType.Enum>
    get() = flatMap(Event::properties)
      .map(Property::type)
      .filterIsInstance<PropertyType.Enum>()
      .distinctBy { enum -> enum.name.rawValue }
      .sortedWith(EnumPropertyNameComparator)

  @Deprecated("Deprecated in Kotlin", level = DeprecationLevel.HIDDEN)
  override fun <T : Any?> toArray(generator: IntFunction<Array<out T?>?>): Array<out T?> {
    @Suppress("DEPRECATION")
    return super.toArray(generator)
  }

  override fun equals(other: Any?): Boolean = other === this || (other is Events && other.entries == this.entries)

  override fun hashCode(): Int = entries.hashCode()

  override fun toString(): String = "Events($entries)"
}

private object EnumPropertyNameComparator : Comparator<PropertyType.Enum> {
  override fun compare(o1: PropertyType.Enum, o2: PropertyType.Enum): Int {
    return o1.name.rawValue.compareTo(o2.name.rawValue)
  }
}
