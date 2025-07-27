package com.automattic.eventhorizon

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe

class EventSpec : FunSpec({
  val description = "Description"
  val properties = buildProperties { text("property") }
  val excludedPlatforms = platforms("android", "ios")

  test("create an event") {
    val event = Event("name", description, properties, excludedPlatforms).shouldBeRight()

    event.name shouldBe caseString("name")
    event.properties shouldHaveSingleElement buildProperty("property")
    event.description shouldBe description
    event.excludedPlatforms shouldBe excludedPlatforms
  }

  test("fail to create an event with an empty name") {
    val result = Event("", description, properties, excludedPlatforms)

    result shouldBeLeft EventProblem.BlankName
  }

  test("fail to create an event with a blank name") {
    val result = Event(" \n ", description, properties, excludedPlatforms)

    result shouldBeLeft EventProblem.BlankName
  }

  test("fail to create an event with duplicate properties") {
    val properties = buildProperties {
      text("name_a")
      boolean("name_a")
      number("name_a")
      text("name_b")
      enum("name_b", enumType("enum_name", "value"))
      boolean("name_c")
    }
    val result = Event("event_name", description, properties, excludedPlatforms)

    result shouldBeLeft EventProblem.DuplicateProperties("event_name", mapOf("name_a" to 3, "name_b" to 2))
  }
})
