package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventSpec : FunSpec({
  test("throw when creating event with duplicate property names") {
    val exception = shouldThrow<IllegalArgumentException> {
      Event(
        "event_name",
        Property.test("name_a", type = PropertyType.Text),
        Property.test("name_a", type = PropertyType.Boolean),
        Property.test("name_a", type = PropertyType.Number),
        Property.test("name_b", type = PropertyType.Text),
        Property.test("name_b", type = PropertyType.Boolean),
        Property.test("name_c", type = PropertyType.Boolean),
      )
    }

    exception shouldHaveMessage "Found duplicate properties for event 'event_name': {name_a=3, name_b=2}"
  }
})
