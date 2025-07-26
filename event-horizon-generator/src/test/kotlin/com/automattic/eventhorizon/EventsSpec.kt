package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.throwable.shouldHaveMessage

class EventsSpec : FunSpec({
  test("fail to create events with duplicate entries") {
    val exception = shouldThrow<IllegalArgumentException> {
      Events(
        listOf(
          buildEvent("event1"),
          buildEvent("event1"),
          buildEvent("event1"),
          buildEvent("event2") {
            properties {
              boolean("property")
            }
          },
          buildEvent("event2") {
            properties {
              text("property")
            }
          },
          buildEvent("event3"),
        ),
      )
    }

    exception shouldHaveMessage "Found duplicate events: {event1=3, event2=2}"
  }

  test("create events without duplicate entries") {
    val events = Events(
      listOf(
        buildEvent("event1"),
        buildEvent("event2"),
      ),
    )

    events shouldContainExactly listOf(
      buildEvent("event1"),
      buildEvent("event2"),
    )
  }

  test("create events with no entries") {
    val events = Events(emptyList())

    events.shouldBeEmpty()
  }

  test("get distinct enums") {
    val events = buildEvents {
      event("event1") {
        properties {
          enum("property1", enumType("enum1", "value1", "value2"))
          enum("property2", enumType("enum2", "value1"))
          enum("property3", enumType("enum3", "value1"))
          enum("property4", enumType("enum3", "value1"))
        }
      }
      event("event2") {
        properties {
          enum("property1", enumType("enum2", "value1"))
          enum("property2", enumType("enum4", "value1", "value2", "value3"))
        }
      }
    }

    events.distinctEnums shouldContainExactly listOf(
      enumType("enum1", "value1", "value2"),
      enumType("enum2", "value1"),
      enumType("enum3", "value1"),
      enumType("enum4", "value1", "value2", "value3"),
    )
  }
})
