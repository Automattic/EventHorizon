package com.automattic.eventhorizon

import com.automattic.eventhorizon.CaseString.Companion.toCaseString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventSpec : FunSpec({
  test("fail to create event with duplicate properties") {
    val exception = shouldThrow<IllegalArgumentException> {
      Event(
        name = "event_name".toCaseString(),
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
