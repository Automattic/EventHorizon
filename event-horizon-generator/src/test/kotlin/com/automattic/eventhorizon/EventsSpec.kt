package com.automattic.eventhorizon

import com.automattic.eventhorizon.Property.Type
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventsSpec : FunSpec({
  test("throw when creating events with duplicate names") {
    val exception = shouldThrow<IllegalArgumentException> {
      Events(
        Event("name_a"),
        Event("name_a", Property("property_name", type = Type.Text)),
        Event("name_a", Property("property_name", type = Type.Boolean)),
        Event("name_b"),
        Event("name_b", Property("property_name", type = Type.Boolean)),
        Event("name_c", Property("property_name", type = Type.Text)),
      )
    }

    exception shouldHaveMessage "Found duplicate events: {name_a=3, name_b=2}"
  }
})
