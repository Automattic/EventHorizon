package com.automattic.eventhorizon

public sealed interface PropertyType {
  public data object Text : PropertyType

  public data object Number : PropertyType

  public data object Boolean : PropertyType

  public data class Enum(
    val name: String,
    val values: Set<String>,
  ) : PropertyType {
    init {
      require(values.isNotEmpty()) { "Enum property '$name' has no values" }
    }

    public companion object {
      public fun test(name: String, value: String, vararg values: String): Enum =
        Enum(name, setOf(value) + values.toSet())
    }
  }
}
