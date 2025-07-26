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
        entries = emptyList(),
      ),
    )

    public fun create(version: ULong, platforms: Set<Platform>, events: Events): Schema {
      require(version > 0u) { "Schema version must not be 0" }
      requirePredeclaredEventPlatforms(events, platforms)
      requirePredeclaredPropertyPlatforms(events, platforms)
      return Schema(
        version = version,
        platforms = platforms,
        events = events,
      )
    }

    private fun requirePredeclaredEventPlatforms(events: Events, platforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidEventPlatforms(platforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Found events with platforms undeclared in schema:\n")
          val eventIssues = invalidPlatforms.joinToString(separator = "\n") { (eventName, platformNames) ->
            " - $eventName: $platformNames"
          }
          append(eventIssues)

          append("\nAvailable platforms:\n")
          val platformNames = platforms.joinToString(separator = "\n") { platform ->
            " - ${platform.value}"
          }
          append(platformNames)
        }
      }
    }

    private fun requirePredeclaredPropertyPlatforms(events: Events, platforms: Set<Platform>) {
      val invalidPlatforms = events.findInvalidPropertyPlatforms(platforms)
      require(invalidPlatforms.isEmpty()) {
        buildString {
          append("Found event properties with platforms undeclared in schema:\n")
          val eventIssues = invalidPlatforms.joinToString(separator = "\n") { (eventName, propertyNames) ->
            val propertyIssues = propertyNames.joinToString(separator = "\n") { (propertyName, platformNames) ->
              "   - $propertyName: $platformNames"
            }
            " - $eventName:\n$propertyIssues"
          }
          append(eventIssues)

          append("\nAvailable platforms:\n")
          val platformNames = platforms.joinToString(separator = "\n") { platform ->
            " - ${platform.value}"
          }
          append(platformNames)
        }
      }
    }

    private fun Events.findInvalidEventPlatforms(availablePlatforms: Set<Platform>) = mapNotNull { event ->
      val invalidPlatforms = event.excludedPlatforms.filter { platform ->
        platform !in availablePlatforms
      }
      if (invalidPlatforms.isNotEmpty()) {
        event.name.rawValue to invalidPlatforms.map(Platform::value)
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
          property.name.rawValue to invalidPlatforms.map(Platform::value)
        } else {
          null
        }
      }
      if (invalidProperties.isNotEmpty()) {
        event.name.rawValue to invalidProperties
      } else {
        null
      }
    }
  }
}
