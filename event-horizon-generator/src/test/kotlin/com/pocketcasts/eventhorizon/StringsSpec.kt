package com.pocketcasts.eventhorizon

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringsSpec : FunSpec({
  test("convert snake_case to camelCase") {
    "this_is_text".snakeToCamelCase() shouldBe "thisIsText"
  }

  test("convert SCREAMING_SNAKE_CASE to camelCase") {
    "THIS_IS_TEXT".snakeToCamelCase() shouldBe "thisIsText"
  }

  test("convert snake_case to PascalCase") {
    "this_is_text".snakeToPascalCase() shouldBe "ThisIsText"
  }

  test("convert SCREAMING_SNAKE_CASE to PascalCase") {
    "THIS_IS_TEXT".snakeToPascalCase() shouldBe "ThisIsText"
  }
})
