package com.automattic.eventhorizon.cli

import com.github.ajalt.clikt.testing.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import kotlin.io.path.writeText

class CliSpec : FunSpec({
  val cli = Cli()
  val inputFile = tempfile().toPath()

  test("verify valid yaml") {
    inputFile.writeText("")

    val result = cli.test("-v", "-i", "$inputFile")

    result.statusCode shouldBe 0
    result.stdout shouldBe """
      |$inputFile file is a valid schema
      |
    """.trimMargin()
  }

  test("verify invalid yaml") {
    inputFile.writeText("??")

    val result = cli.test("-v", "-i", "$inputFile")

    result.statusCode shouldBe 1
    result.output shouldBe """
      |IncorrectTypeException at <root> on line 1, column 1: Expected an object, but got a scalar value
      |
    """.trimMargin()
  }

  test("generate kotlin code") {
    val outputDir = tempdir().toPath()
    inputFile.writeText("")

    val result = cli.test("-i", "$inputFile", "-o", "$outputDir", "-f", "kotlin")

    result.statusCode shouldBe 0
    outputDir.resolve("EventHorizon.kt").shouldExist()
  }

  test("generate swift code") {
    val outputDir = tempdir().toPath()
    inputFile.writeText("")

    val result = cli.test("-i", "$inputFile", "-o", "$outputDir", "-f", "swift")

    result.statusCode shouldBe 0
    outputDir.resolve("EventHorizon.swift").shouldExist()
  }

  test("generate type script code") {
    val outputDir = tempdir().toPath()
    inputFile.writeText("")

    val result = cli.test("-i", "$inputFile", "-o", "$outputDir", "-f", "ts")

    result.statusCode shouldBe 0
    outputDir.resolve("eventHorizon.ts").shouldExist()
  }

  test("generate JSON schema") {
    val outputDir = tempdir().toPath()
    inputFile.writeText("")

    val result = cli.test("-i", "$inputFile", "-o", "$outputDir", "-f", "json")

    result.statusCode shouldBe 0
    outputDir.resolve("event-horizon.json").shouldExist()
  }

  test("require output platform") {
    val outputDir = tempdir().toPath()
    inputFile.writeText(
      """
        |schemaVersion: 1
        |platforms:
        |  - android
      """.trimMargin(),
    )

    val result = cli.test("-i", "$inputFile", "-o", "$outputDir", "-f", "ts")

    result.statusCode shouldBe 1
    result.stderr shouldBe """
      |Usage: event-horizon [<options>]
      |
      |Error: missing option --output-platform
      |
    """.trimMargin()
    outputDir.resolve("eventHorizon.ts").shouldNotExist()
  }

  test("require schema-declared output platform") {
    val outputDir = tempdir().toPath()
    inputFile.writeText(
      """
        |schemaVersion: 1
        |platforms:
        |  - android
        |  - ios
      """.trimMargin(),
    )

    val result = cli.test("-i", "$inputFile", "-o", "$outputDir", "-p", "web", "-f", "ts")

    result.statusCode shouldBe 1
    result.stderr shouldBe """
      |Usage: event-horizon [<options>]
      |
      |Error: Invalid value for --output-platform: web. Must be one of the schema-declared values: [android, ios]
      |
    """.trimMargin()
    outputDir.resolve("eventHorizon.ts").shouldNotExist()
  }

  test("do not require schema-declared output platform when none is defined") {
    val outputDir = tempdir().toPath()
    inputFile.writeText(
      """
        |schemaVersion: 1
      """.trimMargin(),
    )

    val result = cli.test("-i", "$inputFile", "-o", "$outputDir", "-f", "ts")

    result.statusCode shouldBe 0
    outputDir.resolve("eventHorizon.ts").shouldExist()
  }
})
