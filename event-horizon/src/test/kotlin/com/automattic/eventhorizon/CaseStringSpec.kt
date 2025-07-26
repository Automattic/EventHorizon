package com.automattic.eventhorizon

import com.automattic.eventhorizon.CaseString.Companion.toCaseString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class CaseStringSpec : FunSpec({
  test("create cameCase string") {
    val string = "thisIsSomeText1".toCaseString()

    string.rawValue shouldBe "thisIsSomeText1"
    string.case shouldBe Case.Camel
  }

  test("create pascalCase string") {
    val string = "ThisIsSomeText1".toCaseString()

    string.rawValue shouldBe "ThisIsSomeText1"
    string.case shouldBe Case.Pascal
  }

  test("create snake_case string") {
    val string = "this_is_some_text1".toCaseString()

    string.rawValue shouldBe "this_is_some_text1"
    string.case shouldBe Case.Snake
  }

  test("create kebab-case string") {
    val string = "this-is-some-text1".toCaseString()

    string.rawValue shouldBe "this-is-some-text1"
    string.case shouldBe Case.Kebab
  }

  test("create dot.case string") {
    val string = "this.is.some.text1".toCaseString()

    string.rawValue shouldBe "this.is.some.text1"
    string.case shouldBe Case.Dot
  }

  test("use camelCase for lowercase string") {
    val string = "thisissometext1".toCaseString()

    string.case shouldBe Case.Camel
  }

  test("use PascalCase for lowercase string") {
    val string = "THISISSOMETEXT1".toCaseString()

    string.case shouldBe Case.Pascal
  }

  test("ignore character case for snake_case string") {
    val string = "THIS_IS_SOME_TEXT1".toCaseString()

    string.case shouldBe Case.Snake
  }

  test("ignore character case for kebab-case string") {
    val string = "THIS-IS-SOME-TEXT1".toCaseString()

    string.case shouldBe Case.Kebab
  }

  test("keep surrounding separator characters for snake_case string") {
    val string = "_this_is_some_text_".toCaseString()

    string.rawValue shouldBe "_this_is_some_text_"
    string.case shouldBe Case.Snake
  }

  test("keep surrounding separator characters for kebab-case string") {
    val string = "-this-is-some-text-".toCaseString()

    string.rawValue shouldBe "-this-is-some-text-"
    string.case shouldBe Case.Kebab
  }

  test("keep surrounding separator characters for dot.case string") {
    val string = ".this.is.some.text.".toCaseString()

    string.rawValue shouldBe ".this.is.some.text."
    string.case shouldBe Case.Dot
  }

  test("tokenize cameCase string") {
    val tokens = Case.Camel.tokenize("thisIsSomeText")

    tokens shouldContainExactly listOf("this", "is", "some", "text")
  }

  test("tokenize pascalCase string") {
    val tokens = Case.Pascal.tokenize("ThisIsSomeText")

    tokens shouldContainExactly listOf("this", "is", "some", "text")
  }

  test("tokenize snake_case string") {
    val tokens = Case.Snake.tokenize("this_is_some_text")

    tokens shouldContainExactly listOf("this", "is", "some", "text")
  }

  test("tokenize kebab-case string") {
    val tokens = Case.Kebab.tokenize("this-is-some-text")

    tokens shouldContainExactly listOf("this", "is", "some", "text")
  }

  test("tokenize dot.case string") {
    val tokens = Case.Dot.tokenize("this.is.some.text")

    tokens shouldContainExactly listOf("this", "is", "some", "text")
  }

  test("convert to cameCase string") {
    val caseString = "someString".toCaseString()

    val string = caseString.toString(Case.Camel)

    string shouldBe "someString"
  }

  test("convert to pascalCase string") {
    val caseString = "someString".toCaseString()

    val string = caseString.toString(Case.Pascal)

    string shouldBe "SomeString"
  }

  test("convert to snake_case string") {
    val caseString = "someString".toCaseString()

    val string = caseString.toString(Case.Snake)

    string shouldBe "some_string"
  }

  test("convert to kebab-case string") {
    val caseString = "someString".toCaseString()

    val string = caseString.toString(Case.Kebab)

    string shouldBe "some-string"
  }

  test("convert to dot.case string") {
    val caseString = "someString".toCaseString()

    val string = caseString.toString(Case.Dot)

    string shouldBe "some.string"
  }

  test("fail to create unknown case string") {
    val exception = shouldThrow<IllegalArgumentException> {
      "some&string".toCaseString()
    }
    exception shouldHaveMessage """
      |Failed to detect case of 'some&string' string. Supported cases:
      | - Camel
      | - Pascal
      | - Snake
      | - Kebab
      | - Dot
    """.trimMargin()
  }

  test("fail to create mixed case string") {
    val exception = shouldThrow<IllegalArgumentException> {
      "this.is_text".toCaseString()
    }
    exception shouldHaveMessage """
      |Failed to detect case of 'this.is_text' string. Supported cases:
      | - Camel
      | - Pascal
      | - Snake
      | - Kebab
      | - Dot
    """.trimMargin()
  }
})
