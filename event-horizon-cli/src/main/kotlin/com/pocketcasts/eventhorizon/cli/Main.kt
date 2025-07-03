package com.pocketcasts.eventhorizon.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import com.pocketcasts.eventhorizon.json.JsonGenerator
import com.pocketcasts.eventhorizon.kotlin.KotlinGenerator
import com.pocketcasts.eventhorizon.parseEvents
import com.pocketcasts.eventhorizon.swift.SwiftGenerator
import com.pocketcasts.eventhorizon.ts.TypeScriptGenerator

public fun main(vararg args: String) {
  EventHorizonCli().main(args)
}

private class EventHorizonCli : CliktCommand() {
  val inputFile by option("-i", "--input-file")
    .help("Input file with events definitions")
    .path(canBeDir = false, mustExist = true, mustBeReadable = true)
    .required()

  val outputDir by option("-o", "--output-dir")
    .help("Output directory used for generated files")
    .path(canBeFile = false)

  val outputFormat by option("-f", "--output-format")
    .help("Format used for code generation")
    .enum<FormatType>(key = FormatType::optionName)

  val namespace by option("-n", "--namespace")
    .help("Namespace used for generated code, if applicable")
    .default("")

  val onlyVerify by option("-v", "--verify")
    .help("Only run input file verification")
    .flag()

  override fun run() {
    if (onlyVerify) {
      verify()
    } else {
      generate()
    }
  }

  private fun verify() {
    parseEvents(inputFile)
      .onSuccess { echo("No issues found in $inputFile file") }
      .getOrThrow()
  }

  private fun generate() {
    val format = requireOption(outputFormat) { "missing option --output-format" }
    val dir = requireOption(outputDir) { "missing option --output-dir" }
    parseEvents(inputFile)
      .map { events -> createGenerator(format).generate(events, dir) }
      .onSuccess { file -> echo("$file file generated successfully") }
      .getOrThrow()
  }

  private fun createGenerator(formatType: FormatType) = when (formatType) {
    FormatType.Kotlin -> KotlinGenerator(namespace)
    FormatType.Swift -> SwiftGenerator(namespace)
    FormatType.TypeScript -> TypeScriptGenerator()
    FormatType.Json -> JsonGenerator()
  }
}

private fun <T : Any> requireOption(value: T?, message: () -> String): T {
  if (value == null) {
    throw UsageError(message())
  }
  return value
}

private enum class FormatType(
  val optionName: String,
) {
  Kotlin(
    optionName = "kotlin",
  ),
  Swift(
    optionName = "swift",
  ),
  TypeScript(
    optionName = "ts",
  ),
  Json(
    optionName = "json",
  ),
}
