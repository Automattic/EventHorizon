package com.automattic.eventhorizon

public data class Property(
  val name: CaseString,
  val type: PropertyType,
  val description: String?,
  val optionalPlatforms: Set<Platform>,
) {
  public fun isOptional(platform: Platform): Boolean = platform in optionalPlatforms
}
