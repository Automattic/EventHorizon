package com.automattic.eventhorizon.cli

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlin.io.path.writeText

class CliSpec : FunSpec({
  val inputFile = tempfile().toPath()
  val outputDir = tempdir().toPath()

  test("verify valid yaml") {
    inputFile.writeText("")

    shouldNotThrowAny {
      main("-v", "-i", "$inputFile")
    }
  }

  test("verify invalid yaml") {
    inputFile.writeText("??")

    val exception = shouldThrowAny {
      main("-v", "-i", "$inputFile")
    }
    exception shouldHaveMessage "Expected an object, but got a scalar value"
  }

  test("generate kotlin") {
    inputFile.writeText("")

    main("-i", "$inputFile", "-o", "$outputDir", "-f", "kotlin")

    outputDir.resolve("EventHorizon.kt").shouldExist()
  }

  test("generate swift") {
    inputFile.writeText("")

    main("-i", "$inputFile", "-o", "$outputDir", "-f", "swift")

    outputDir.resolve("EventHorizon.swift").shouldExist()
  }

  test("generate type script") {
    inputFile.writeText("")

    main("-i", "$inputFile", "-o", "$outputDir", "-f", "ts")

    outputDir.resolve("eventHorizon.ts").shouldExist()
  }

  test("generate JSON") {
    inputFile.writeText("")

    main("-i", "$inputFile", "-o", "$outputDir", "-f", "json")

    outputDir.resolve("event-horizon.json").shouldExist()
  }

  test("require schema-declared output platform") {
    inputFile.writeText(
      """
        |version: 1
        |platforms:
        |  - android
      """.trimMargin(),
    )

    val exception = shouldThrowAny {
      main("-i", "$inputFile", "-o", "$outputDir", "-f", "ts")
    }
    exception shouldHaveMessage "missing option --output-platform"
  }

  test("do not require schema-declared output platform when none is defined") {
    inputFile.writeText(
      """
        |version: 1
      """.trimMargin(),
    )

    main("-i", "$inputFile", "-o", "$outputDir", "-f", "ts")

    outputDir.resolve("eventHorizon.ts").shouldExist()
  }
})
