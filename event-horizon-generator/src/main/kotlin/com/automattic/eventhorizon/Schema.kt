package com.automattic.eventhorizon

@ConsistentCopyVisibility
public data class Schema private constructor(
  val version: ULong,
  val platforms: Set<Platform>,
  val events: Events,
) {
  public fun platformEvents(platform: Platform): Events {
    return Events(events.filter { event -> platform !in event.excludedPlatforms })
  }

  public companion object {
    public val Empty: Schema = Schema(
      version = 0u,
      platforms = emptySet(),
      events = Events(
        value = emptyList(),
      ),
    )

    public fun create(version: ULong, platforms: Set<Platform>, events: Events): Schema {
      require(version > 0u) { "Schema version must be a positive number. Is: $version" }
      requirePredeclaredEventPlatforms(events, platforms)
      requirePredeclaredPropertyPlatforms(events, platforms)
      return Schema(
        version = version,
        platforms = platforms,
        events = events,
      )
    }

    private fun requirePredeclaredEventPlatforms(events: Events, plaforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidEventPlatforms(plaforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Schema must declare platforms for excluded events. Available platforms:\n")
          val platforms = plaforms.joinToString(separator = "\n") { platform ->
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

    private fun requirePredeclaredPropertyPlatforms(events: Events, platforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidPropertyPlatforms(platforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Schema must declare platforms for optional properties. Available platforms:\n")
          val platformNames = platforms.joinToString(separator = "\n") { platform ->
            " - ${platform.value}"
          }
          append(platformNames)

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
      val invalidPlatforms = event.excludedPlatforms.filter { platform ->
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
