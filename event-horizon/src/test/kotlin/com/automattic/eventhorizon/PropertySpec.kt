package com.automattic.eventhorizon

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PropertySpec : FunSpec({
  val description = "Description"
  val optionalPlatforms = platforms("android", "ios")

  test("create a property") {
    val property = Property("name", PropertyType.Text, description, optionalPlatforms).shouldBeRight()

    property.name shouldBe caseString("name")
    property.type shouldBe PropertyType.Text
    property.description shouldBe description
    property.optionalPlatforms shouldBe optionalPlatforms
  }

  test("fail to create a property with an empty name") {
    val result = Property("", PropertyType.Text, description, optionalPlatforms)

    result shouldBeLeft PropertyProblem.BlankName
  }

  test("fail to create a property with a blank name") {
    val result = Property(" \n ", PropertyType.Text, description, optionalPlatforms)

    result shouldBeLeft PropertyProblem.BlankName
  }

  test("fail to create a property with unsupported name casing") {
    val result = Property("name+1", PropertyType.Text, description, optionalPlatforms)

    result shouldBeLeft PropertyProblem.UnknownNameCase("name+1")
  }
})
