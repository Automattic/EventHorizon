package com.automattic.eventhorizon

public data class Property(
  val name: String,
  val type: PropertyType,
  val documentation: String?,
  val optionalPlatforms: Set<Platform>,
) {
  public fun isOptional(platform: Platform): Boolean = platform in optionalPlatforms
}
