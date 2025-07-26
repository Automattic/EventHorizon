package com.automattic.eventhorizon

@ConsistentCopyVisibility
public data class Schema private constructor(
  val schemaVersion: ULong,
  val availablePlatforms: Set<Platform>,
  val events: Events,
) {
  public companion object {
    public val Empty: Schema = Schema(
      schemaVersion = 0u,
      availablePlatforms = emptySet(),
      events = Events(
        value = emptyList(),
      ),
    )

    public fun create(schemaVersion: ULong, availablePlatforms: Set<Platform>, events: Events): Schema {
      require(schemaVersion > 0u) { "Schema version must be a positive number. Is: $schemaVersion" }
      requirePredeclaredEventPlatforms(events, availablePlatforms)
      requirePredeclaredPropertyPlatforms(events, availablePlatforms)
      return Schema(
        schemaVersion = schemaVersion,
        availablePlatforms = availablePlatforms,
        events = events,
      )
    }

    private fun requirePredeclaredEventPlatforms(events: Events, availablePlatforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidEventPlatforms(availablePlatforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Schema must declare platforms for optional events. Available platforms:\n")
          val platforms = availablePlatforms.joinToString(separator = "\n") { platform ->
            " - ${platform.value}"
          }
          append(platforms)

          append("\nIssues found with the following events:\n")
          val eventIssues = invalidPlatforms.joinToString(separator = "\n") { (eventName, platformNames) ->
            " - $eventName: $platformNames"
          }
          append(eventIssues)
        }
      }
    }

    private fun requirePredeclaredPropertyPlatforms(events: Events, availablePlatforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidPropertyPlatforms(availablePlatforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Schema must declare platforms for optional properties. Available platforms:\n")
          val platforms = availablePlatforms.joinToString(separator = "\n") { platform ->
            " - ${platform.value}"
          }
          append(platforms)

          append("\nIssues found with the following events and properties:\n")
          val eventIssues = invalidPlatforms.joinToString(separator = "\n") { (eventName, propertyNames) ->
            val propertyIssues = propertyNames.joinToString(separator = "\n") { (propertyName, platformNames) ->
              "   - $propertyName: $platformNames"
            }
            " - $eventName:\n$propertyIssues"
          }
          append(eventIssues)
        }
      }
    }

    private fun Events.findInvalidEventPlatforms(availablePlatforms: Set<Platform>) = mapNotNull { event ->
      val invalidPlatforms = event.availablePlatforms.filter { platform ->
        platform !in availablePlatforms
      }
      if (invalidPlatforms.isNotEmpty()) {
        event.name to invalidPlatforms.map(Platform::value)
      } else {
        null
      }
    }

    private fun Events.findInvalidPropertyPlatforms(availablePlatforms: Set<Platform>) = mapNotNull { event ->
      val invalidProperties = event.properties.mapNotNull { property ->
        val invalidPlatforms = property.optionalPlatforms.filter { platform ->
          platform !in availablePlatforms
        }
        if (invalidPlatforms.isNotEmpty()) {
          property.name to invalidPlatforms.map(Platform::value)
        } else {
          null
        }
      }
      if (invalidProperties.isNotEmpty()) {
        event.name to invalidProperties
      } else {
        null
      }
    }
  }
}
