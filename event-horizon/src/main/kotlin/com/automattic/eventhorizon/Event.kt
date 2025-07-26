package com.automattic.eventhorizon

public data class Event(
  val name: CaseString,
  val description: String?,
  val excludedPlatforms: Set<Platform>,
  val properties: List<Property>,
) {
  init {
    requireNoDuplicates(properties.map(Property::name)) { duplicates ->
      "Found duplicate properties for event '${name.rawValue}': $duplicates"
    }
  }
}
