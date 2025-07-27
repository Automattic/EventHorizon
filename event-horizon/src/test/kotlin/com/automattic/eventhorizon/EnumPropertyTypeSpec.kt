package com.automattic.eventhorizon

import arrow.core.nonEmptyListOf
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe

class EnumPropertyTypeSpec : FunSpec({
  test("create an enum") {
    val enum = PropertyType.Enum("enum_name", setOf("value1")).shouldBeRight()

    enum.name shouldBe caseString("enum_name")
    enum.values shouldHaveSingleElement caseString("value1")
  }

  test("fail to create an enum with an empty name") {
    val result = PropertyType.Enum("", setOf("value1"))

    result shouldBeLeft EnumTypeProblem.BlankName
  }

  test("fail to create an enum with a blank name") {
    val result = PropertyType.Enum(" \n ", setOf("value1"))

    result shouldBeLeft EnumTypeProblem.BlankName
  }

  test("fail to create an enum with unsupported name casing") {
    val result = PropertyType.Enum("enum!name", setOf("value1"))

    result shouldBeLeft EnumTypeProblem.UnknownNameCase("enum!name")
  }

  test("fail to create an enum with no values") {
    val result = PropertyType.Enum("enum_name", emptySet())

    result shouldBeLeft EnumTypeProblem.NoValues("enum_name")
  }

  test("fail to create an enum with empty values") {
    val result = PropertyType.Enum("enum_name", setOf("value", ""))

    result shouldBeLeft EnumTypeProblem.BlankValues("enum_name")
  }

  test("fail to create an enum with blank values") {
    val result = PropertyType.Enum("enum_name", setOf("value", " \n "))

    result shouldBeLeft EnumTypeProblem.BlankValues("enum_name")
  }

  test("fail to create an enum with unsupported value casings") {
    val values = setOf(
      "value1",
      "value!2",
      "value+3",
    )

    val result = PropertyType.Enum("enum_name", values)

    result shouldBeLeft EnumTypeProblem.UnknownValueCases("enum_name", nonEmptyListOf("value!2", "value+3"))
  }
})
