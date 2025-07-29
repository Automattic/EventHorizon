package com.automattic.eventhorizon

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class SchemaSpec : FunSpec({
  test("create a schema") {
    val version = 1uL
    val platforms = platforms("android", "ios")
    val events = buildEvents {
      event("event1")
      event("event2") {
        excludedPlatforms("android")
      }
    }
    val schema = Schema(version, platforms, events).shouldBeRight()

    schema.version shouldBe version
    schema.platforms shouldBe platforms
    schema.events shouldBe events
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

  test("get platform specific enums") {
    val schema = buildSchema {
      platforms("android", "ios", "web")
      events {
        event("event1") {
          excludedPlatforms("android")
          properties {
            enum("property1", enumType("enumName1", "value"))
            enum("property2", enumType("enumName2", "value"))
          }
        }
        event("event2") {
          excludedPlatforms("ios")
          properties {
            enum("property1", enumType("enumName1", "value"))
            enum("property2", enumType("enumName3", "value1", "value2"))
          }
        }
        event("event3") {
          properties {
            enum("property1", enumType("enumName3", "value1", "value2"))
            enum("property2", enumType("enumName4", "value"))
          }
        }
      }
    }

    schema.platformEnums(Platform("android")) shouldContainExactly listOf(
      enumType("enumName1", "value"),
      enumType("enumName3", "value1", "value2"),
      enumType("enumName4", "value"),
    )
  }

  test("empty schema has version 0") {
    Schema.empty.version shouldBe 0u
  }

  test("fail to create a schema with version 0") {
    val result = Schema(version = 0u, platforms(), buildEvents())

    result shouldBeLeft SchemaProblem.InvalidSchemaVersion(0u)
  }

  test("fail to create a schema with unsupported version") {
    val version = Schema.supportedVersions.max() + 1u
    val result = Schema(version, platforms(), buildEvents())

    result shouldBeLeft SchemaProblem.InvalidSchemaVersion(version)
  }

  test("fail to create a schema with duplicate events") {
    val events = buildEvents {
      event("event1")
      event("event1") {
        properties {
          text("property1")
        }
      }
      event("event2")
      event("event2")
      event("event2")
      event("event3")
    }
    val result = Schema(version = 1u, platforms(), events)

    result shouldBeLeft SchemaProblem.DuplicateEvents(mapOf("event1" to 2, "event2" to 3))
  }

  test("fail to create a schema with undeclared platforms used in events") {
    val result = Schema(
      version = 1u,
      platforms = platforms("android", "ios"),
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

    result shouldBeLeft SchemaProblem.UnknownEventPlatforms(
      unknownPlatforms = mapOf(
        "event2" to listOf("web"),
        "event3" to listOf("web", "embedded"),
      ),
      availablePlatforms = listOf("android", "ios"),
    )
  }

  test("fail to create a schema with undeclared platforms used in properties") {
    val result = Schema(
      version = 1u,
      platforms = platforms("android", "ios"),
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

    result shouldBeLeft SchemaProblem.UnknownPropertyPlatforms(
      unknownPlatforms = mapOf(
        "event3" to mapOf(
          "prop1" to listOf("web"),
        ),
        "event4" to mapOf(
          "prop1" to listOf("web"),
          "prop2" to listOf("desktop"),
        ),
        "event5" to mapOf(
          "prop1" to listOf("web", "embedded"),
        ),
      ),
      availablePlatforms = listOf("android", "ios"),
    )
  }

  test("fail to create a schema with inconsistent enums") {
    val result = Schema(
      version = 1u,
      platforms = platforms(),
      events = buildEvents {
        event("event1") {
          properties {
            enum("prop1", enumType("enum1", "value_1_1"))
            enum("prop2", enumType("enum1", "value_1_1", "value_1_2"))
            enum("prop3", enumType("enum2", "value_2_1"))
            enum("prop4", enumType("enum3", "value_3_1"))
            enum("prop5", enumType("enum3", "value_3_1"))
          }
        }
        event("event2") {
          properties {
            enum("prop1", enumType("enum1", "value_1_1"))
            enum("prop2", enumType("enum2", "value_2_1", "value_2_2", "value_2_3"))
            enum("prop3", enumType("enum3", "value_3_1"))
          }
        }
      },
    )

    result shouldBeLeft SchemaProblem.InconsistentEnumValues(
      mapOf(
        "enum1" to listOf(
          listOf("value_1_1"),
          listOf("value_1_1", "value_1_2"),
        ),
        "enum2" to listOf(
          listOf("value_2_1"),
          listOf("value_2_1", "value_2_2", "value_2_3"),
        ),
      ),
    )
  }
})
