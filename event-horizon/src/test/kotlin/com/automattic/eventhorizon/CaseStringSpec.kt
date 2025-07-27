package com.automattic.eventhorizon

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

class CaseStringSpec : FunSpec({
  test("create camelCase string") {
    val string = CaseString("thisIsSomeText1").shouldBeRight()

    string.rawValue shouldBe "thisIsSomeText1"
    string.case shouldBe Case.Camel
  }

  test("create pascalCase string") {
    val string = CaseString("ThisIsSomeText1").shouldBeRight()

    string.rawValue shouldBe "ThisIsSomeText1"
    string.case shouldBe Case.Pascal
  }

  test("create snake_case string") {
    val string = CaseString("this_is_some_text1").shouldBeRight()

    string.rawValue shouldBe "this_is_some_text1"
    string.case shouldBe Case.Snake
  }

  test("create kebab-case string") {
    val string = CaseString("this-is-some-text1").shouldBeRight()

    string.rawValue shouldBe "this-is-some-text1"
    string.case shouldBe Case.Kebab
  }

  test("create dot.case string") {
    val string = CaseString("this.is.some.text1").shouldBeRight()

    string.rawValue shouldBe "this.is.some.text1"
    string.case shouldBe Case.Dot
  }

  test("use camelCase for lowercase string") {
    val string = CaseString("thisissometext1").shouldBeRight()

    string.case shouldBe Case.Camel
  }

  test("use PascalCase for lowercase string") {
    val string = CaseString("THISISSOMETEXT1").shouldBeRight()

    string.case shouldBe Case.Pascal
  }

  test("ignore character case for snake_case string") {
    val string = CaseString("THIS_IS_SOME_TEXT1").shouldBeRight()

    string.case shouldBe Case.Snake
  }

  test("ignore character case for kebab-case string") {
    val string = CaseString("THIS-IS-SOME-TEXT1").shouldBeRight()

    string.case shouldBe Case.Kebab
  }

  test("keep surrounding separator characters for snake_case string") {
    val string = CaseString("_this_is_some_text_").shouldBeRight()

    string.rawValue shouldBe "_this_is_some_text_"
    string.case shouldBe Case.Snake
  }

  test("keep surrounding separator characters for kebab-case string") {
    val string = CaseString("-this-is-some-text-").shouldBeRight()

    string.rawValue shouldBe "-this-is-some-text-"
    string.case shouldBe Case.Kebab
  }

  test("keep surrounding separator characters for dot.case string") {
    val string = CaseString(".this.is.some.text.").shouldBeRight()

    string.rawValue shouldBe ".this.is.some.text."
    string.case shouldBe Case.Dot
  }

  test("tokenize camelCase string") {
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

  test("convert to camelCase string") {
    val caseString = CaseString("someString").shouldBeRight()

    val string = caseString.toString(Case.Camel)

    string shouldBe "someString"
  }

  test("convert to pascalCase string") {
    val caseString = CaseString("someString").shouldBeRight()

    val string = caseString.toString(Case.Pascal)

    string shouldBe "SomeString"
  }

  test("convert to snake_case string") {
    val caseString = CaseString("someString").shouldBeRight()

    val string = caseString.toString(Case.Snake)

    string shouldBe "some_string"
  }

  test("convert to kebab-case string") {
    val caseString = CaseString("someString").shouldBeRight()

    val string = caseString.toString(Case.Kebab)

    string shouldBe "some-string"
  }

  test("convert to dot.case string") {
    val caseString = CaseString("someString").shouldBeRight()

    val string = caseString.toString(Case.Dot)

    string shouldBe "some.string"
  }

  test("fail to create unknown case string") {
    CaseString("some&string") shouldBeLeft "some&string"
  }

  test("fail to create mixed case string") {
    CaseString("this.is_text") shouldBeLeft "this.is_text"
  }
})
