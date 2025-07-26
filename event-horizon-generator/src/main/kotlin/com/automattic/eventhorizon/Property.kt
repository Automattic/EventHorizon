package com.automattic.eventhorizon

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
