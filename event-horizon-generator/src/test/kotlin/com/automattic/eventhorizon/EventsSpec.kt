package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventsSpec : FunSpec({
  test("throw when creating events with duplicate names") {
    val exception = shouldThrow<IllegalArgumentException> {
      Events(
        Event("name_a"),
        Event("name_a", Property.test("property_name", type = PropertyType.Text)),
        Event("name_a", Property.test("property_name", type = PropertyType.Boolean)),
        Event("name_b"),
        Event("name_b", Property.test("property_name", type = PropertyType.Boolean)),
        Event("name_c", Property.test("property_name", type = PropertyType.Text)),
      )
    }

    exception shouldHaveMessage "Found duplicate events: {name_a=3, name_b=2}"
  }
})
