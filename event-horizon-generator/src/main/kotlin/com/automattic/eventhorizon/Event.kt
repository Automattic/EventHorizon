package com.automattic.eventhorizon

public data class Event(
  val name: String,
  val documentation: String?,
  val properties: List<Property>,
  val excludedPlatforms: Set<Platform>,
) {
  init {
    requireNoDuplicates(properties.map(Property::name)) { duplicates ->
      "Found duplicate properties for event '$name': $duplicates"
    }
  }
}
