package com.pocketcasts.eventhorizon

import com.pocketcasts.eventhorizon.Property.Type
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventSpec : FunSpec({
  test("throw when creating event with duplicate property names") {
    val exception = shouldThrow<IllegalArgumentException> {
      Event(
        "event_name",
        Property("name_a", Type.Text),
        Property("name_a", Type.Boolean),
        Property("name_a", Type.Number),
        Property("name_b", Type.Text),
        Property("name_b", Type.Boolean),
        Property("name_c", Type.Boolean),
      )
    }

    exception shouldHaveMessage "Found duplicate properties for event 'event_name': {name_a=3, name_b=2}"
  }
})
