package com.automattic.eventhorizon.cli

import com.automattic.eventhorizon.EventHorizonSchema
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.json.JsonGenerator
import com.automattic.eventhorizon.kotlin.KotlinGenerator
import com.automattic.eventhorizon.parseSchema
import com.automattic.eventhorizon.swift.SwiftGenerator
import com.automattic.eventhorizon.ts.TypeScriptGenerator
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.parse
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path

public fun main(vararg args: String) {
  val cli = EventHorizonCli()
  try {
    cli.parse(args.asList())
  } catch (e: CliktError) {
    cli.echoFormattedHelp(e)
    throw e
  }
}

private class EventHorizonCli : CliktCommand() {
  val inputFile by option("-i", "--input-file")
    .help("Input file with events definitions")
    .path(canBeDir = false, mustExist = true, mustBeReadable = true)
    .required()

  val outputDir by option("-o", "--output-dir")
    .help("Output directory used for generated files")
    .path(canBeFile = false)

  val outputPlatform by option("-p", "--output-platform")
    .help("Output platform for code generation")
    .convert { Platform(it) }

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
    parseSchema(inputFile)
      .onSuccess { echo("No issues found in $inputFile file") }
      .getOrThrow()
  }

  private fun generate() {
    val format = requireOption(outputFormat) { "missing option --output-format" }
    val dir = requireOption(outputDir) { "missing option --output-dir" }
    parseSchema(inputFile)
      .mapCatching(::requireDeclaredPlatform)
      .map { schema -> createGenerator(format, outputPlatform ?: Platform.NoPlatform).generate(schema, dir) }
      .onSuccess { file -> echo("$file file generated successfully") }
      .getOrThrow()
  }

  private fun createGenerator(formatType: FormatType, platform: Platform) = when (formatType) {
    FormatType.Kotlin -> KotlinGenerator(namespace, platform)
    FormatType.Swift -> SwiftGenerator(namespace, platform)
    FormatType.TypeScript -> TypeScriptGenerator(platform)
    FormatType.Json -> JsonGenerator()
  }

  private fun requireDeclaredPlatform(schema: EventHorizonSchema): EventHorizonSchema {
    if (schema.availablePlatforms.isNotEmpty()) {
      val platform = requireOption(outputPlatform) { "missing option --output-platform" }
      if (schema.availablePlatforms.isNotEmpty() && platform !in schema.availablePlatforms) {
        throw UsageError(
          "Invalid value for --output-platform: ${platform.value}. It must be one of the schema-declared values: ${schema.availablePlatforms}",
        )
      }
    }
    return schema
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
