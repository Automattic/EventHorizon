package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class EventHorizonSchemaSpec() : FunSpec({
  test("schema with version 0") {
    val exception = shouldThrow<IllegalArgumentException> {
      EventHorizonSchema.create(
        schemaVersion = 0u,
        availablePlatforms = emptySet(),
        events = Events(),
      )
    }
    exception shouldHaveMessage "Schema version must be a positive number. Is: 0"
  }

  test("empty schema has version 0") {
    EventHorizonSchema.Empty.schemaVersion shouldBe 0u
  }

  test("schema with missing property platforms") {
    val exception = shouldThrow<IllegalArgumentException> {
      EventHorizonSchema.create(
        schemaVersion = 1u,
        availablePlatforms = setOf(Platform("android"), Platform("ios")),
        events = Events(
          Event("event1", Property.test("prop1")),
          Event("event2", Property.test("prop1", optionalPlatforms = setOf("android"))),
          Event(
            "event3",
            Property.test("prop1", optionalPlatforms = setOf("web")),
            Property.test("prop2", optionalPlatforms = setOf("android")),
          ),
          Event(
            "event4",
            Property.test("prop1", optionalPlatforms = setOf("web")),
            Property.test("prop2", optionalPlatforms = setOf("desktop")),
          ),
          Event("event5", Property.test("prop1", optionalPlatforms = setOf("web", "embedded"))),
        ),
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
