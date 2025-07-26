package com.automattic.eventhorizon

public data class Property(
  val name: String,
  val type: PropertyType,
  val documentation: String?,
  val optionalPlatforms: Set<Platform>,
) {
  public fun isOptional(platform: Platform): Boolean = platform in optionalPlatforms

  public companion object {
    public fun test(
      name: String,
      type: PropertyType = PropertyType.Text,
      documentation: String? = null,
      optionalPlatforms: Set<String> = emptySet(),
    ): Property = Property(
      name = name,
      type = type,
      documentation = documentation,
      optionalPlatforms = optionalPlatforms.mapTo(mutableSetOf(), ::Platform),
    )
  }
}
