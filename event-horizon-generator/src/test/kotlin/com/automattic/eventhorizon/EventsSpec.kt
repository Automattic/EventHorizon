package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EventsSpec : FunSpec({
  test("throw when creating events with duplicate names") {
    val exception = shouldThrow<IllegalArgumentException> {
      Events(
        listOf(
          buildEvent("name_a"),
          buildEvent("name_a"),
          buildEvent("name_a"),
          buildEvent("name_b"),
          buildEvent("name_b"),
          buildEvent("name_c"),
        ),
      )
    }

    exception shouldHaveMessage "Found duplicate events: {name_a=3, name_b=2}"
  }
})
