package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventSpec : FunSpec({
  test("throw when creating event with duplicate property names") {
    val exception = shouldThrow<IllegalArgumentException> {
      Event(
        name = "event_name",
        properties = buildProperties {
          text("name_a")
          boolean("name_a")
          number("name_a")
          text("name_b")
          enum("name_b", enumType("enum_name", "value"))
          boolean("name_c")
        },
        description = null,
        excludedPlatforms = emptySet(),
      )
    }

    exception shouldHaveMessage "Found duplicate properties for event 'event_name': {name_a=3, name_b=2}"
  }
})
