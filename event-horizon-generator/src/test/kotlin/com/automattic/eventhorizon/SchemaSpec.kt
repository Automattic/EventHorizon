package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class SchemaSpec() : FunSpec({
  test("schema with version 0") {
    val exception = shouldThrow<IllegalArgumentException> {
      Schema.create(
        schemaVersion = 0u,
        availablePlatforms = emptySet(),
        events = buildEvents(),
      )
    }
    exception shouldHaveMessage "Schema version must be a positive number. Is: 0"
  }

  test("empty schema has version 0") {
    Schema.Empty.schemaVersion shouldBe 0u
  }

  test("schema with missing event platforms") {
    val exception = shouldThrow<IllegalArgumentException> {
      Schema.create(
        schemaVersion = 1u,
        availablePlatforms = setOf(Platform("android"), Platform("ios")),
        events = buildEvents {
          event("event1") {
            availablePlatforms("android")
          }
          event("event2") {
            availablePlatforms("ios", "web")
          }
          event("event3") {
            availablePlatforms("web", "embedded")
          }
        },
      )
    }
    exception shouldHaveMessage """
      |Schema must declare platforms for optional events. Available platforms:
      | - android
      | - ios
      |Issues found with the following events:
      | - event2: [web]
      | - event3: [web, embedded]
    """.trimMargin()
  }

  test("schema with missing property platforms") {
    val exception = shouldThrow<IllegalArgumentException> {
      Schema.create(
        schemaVersion = 1u,
        availablePlatforms = setOf(Platform("android"), Platform("ios")),
        events = buildEvents {
          event("event1")
          event("event2") {
            properties {
              text("prop1") {
                optionalPlatforms("android")
              }
            }
          }
          event("event3") {
            properties {
              text("prop1") {
                optionalPlatforms("web")
              }
              text("prop2") {
                optionalPlatforms("android")
              }
            }
          }
          event("event4") {
            properties {
              text("prop1") {
                optionalPlatforms("web")
              }
              text("prop2") {
                optionalPlatforms("desktop")
              }
            }
          }
          event("event5") {
            properties {
              text("prop1") {
                optionalPlatforms("web", "embedded")
              }
            }
          }
        },
      )
    }
    exception shouldHaveMessage """
      |Schema must declare platforms for optional properties. Available platforms:
      | - android
      | - ios
      |Issues found with the following events and properties:
      | - event3:
      |   - prop1: [web]
      | - event4:
      |   - prop1: [web]
      |   - prop2: [desktop]
      | - event5:
      |   - prop1: [web, embedded]
    """.trimMargin()
  }
})
