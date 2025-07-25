package com.automattic.eventhorizon

import com.automattic.eventhorizon.Property.Type
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventSpec : FunSpec({
  test("throw when creating event with duplicate property names") {
    val exception = shouldThrow<IllegalArgumentException> {
      Event(
        "event_name",
        Property.test("name_a", type = Type.Text),
        Property.test("name_a", type = Type.Boolean),
        Property.test("name_a", type = Type.Number),
        Property.test("name_b", type = Type.Text),
        Property.test("name_b", type = Type.Boolean),
        Property.test("name_c", type = Type.Boolean),
      )
    }

    exception shouldHaveMessage "Found duplicate properties for event 'event_name': {name_a=3, name_b=2}"
  }
})
