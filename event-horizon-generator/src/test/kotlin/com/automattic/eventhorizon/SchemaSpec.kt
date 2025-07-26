package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class SchemaSpec() : FunSpec({
  test("empty schema has version 0") {
    Schema.Empty.version shouldBe 0u
  }

  test("fail to create schema with version 0") {
    val exception = shouldThrow<IllegalArgumentException> {
      Schema.create(
        version = 0u,
        platforms = emptySet(),
        events = buildEvents(),
      )
    }
    exception shouldHaveMessage "Schema version must not be 0"
  }

  test("create schema with positive version number") {
    val schema = Schema.create(
      version = 100u,
      platforms = emptySet(),
      events = buildEvents(),
    )

    schema.version shouldBe 100u
  }

  test("get platform specific events") {
    val schema = buildSchema {
      platforms("android", "ios", "web")
      events {
        event("event1") {
          excludedPlatforms("android")
        }
        event("event2") {
          excludedPlatforms("ios")
        }
        event("event3") {
          excludedPlatforms("android", "ios")
        }
      }
    }

    schema.platformEvents(Platform("android")) shouldContainExactly buildEvents {
      event("event2") {
        excludedPlatforms("ios")
      }
    }
    schema.platformEvents(Platform("web")) shouldContainExactly buildEvents {
      event("event1") {
        excludedPlatforms("android")
      }
      event("event2") {
        excludedPlatforms("ios")
      }
      event("event3") {
        excludedPlatforms("android", "ios")
      }
    }
  }

  test("fail to create schema with undeclared platforms used in events") {
    val exception = shouldThrow<IllegalArgumentException> {
      Schema.create(
        version = 1u,
        platforms = setOf(Platform("android"), Platform("ios")),
        events = buildEvents {
          event("event1") {
            excludedPlatforms("android")
          }
          event("event2") {
            excludedPlatforms("ios", "web")
          }
          event("event3") {
            excludedPlatforms("web", "embedded")
          }
        },
      )
    }
    exception shouldHaveMessage """
      |Found events with platforms undeclared in schema:
      | - event2: [web]
      | - event3: [web, embedded]
      |Available platforms:
      | - android
      | - ios
    """.trimMargin()
  }

  test("fail to create schema with undeclared platforms used in properties") {
    val exception = shouldThrow<IllegalArgumentException> {
      Schema.create(
        version = 1u,
        platforms = setOf(Platform("android"), Platform("ios")),
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
      |Found event properties with platforms undeclared in schema:
      | - event3:
      |   - prop1: [web]
      | - event4:
      |   - prop1: [web]
      |   - prop2: [desktop]
      | - event5:
      |   - prop1: [web, embedded]
      |Available platforms:
      | - android
      | - ios
    """.trimMargin()
  }
})
