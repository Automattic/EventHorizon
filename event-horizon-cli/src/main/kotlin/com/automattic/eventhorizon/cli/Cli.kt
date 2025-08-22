package com.automattic.eventhorizon.cli

import com.automattic.eventhorizon.Generator
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Problem
import com.automattic.eventhorizon.Schema
import com.automattic.eventhorizon.YamlParser
import com.automattic.eventhorizon.json.JsonGenerator
import com.automattic.eventhorizon.kotlin.KotlinGenerator
import com.automattic.eventhorizon.swift.SwiftGenerator
import com.automattic.eventhorizon.ts.TypeScriptGenerator
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.ProgramResult
import com.github.ajalt.clikt.core.UsageError
import com.github.ajalt.clikt.core.theme
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import java.text.Format
import kotlin.io.path.isDirectory

internal class Cli : CliktCommand("event-horizon") {
  private val inputFile by option("-i", "--input-file")
    .help("Input file with events definitions")
    .path(canBeDir = false, mustExist = true, mustBeReadable = true)
    .required()

  private val outputPath by option("-o", "--output-path")
    .help("Output path used for generated files. Code generation must use a directory. It can be a file for JSON generation.")
    .path()

  private val outputPlatform by option("-p", "--output-platform")
    .help("Platform used for code generation")
    .convert { Platform(it) }

  private val outputFormat by option("-f", "--output-format")
    .help("Format used for code generation")
    .enum<FormatType>(key = FormatType::optionName)

  private val namespace by option("-n", "--namespace")
    .help("Namespace used for generated code, if applicable")
    .default("")

  private val onlyVerify by option("-v", "--verify")
    .help("Only run input file verification")
    .flag()

  override fun help(context: Context): String {
    return context.theme.info("This is a command")
  }

  override fun run() {
    if (onlyVerify) {
      verify()
    } else {
      generate()
    }
  }

  private fun verify() {
    YamlParser().parseSchema(inputFile)
      .onRight { echoSuccess("$inputFile file is a valid schema") }
      .onLeft(::echoProblemsAndExit)
  }

  private fun generate() {
    val format = requireOption(outputFormat) { "missing option --output-format" }
    val path = requireOption(outputPath) { "missing option --output-path" }
    requireCorrectOutput(path, format)

    YamlParser()
      .parseSchema(inputFile)
      .map { schema -> requireDeclaredPlatform(schema, format) }
      .map { (schema, platform) ->
        createGenerator(format, platform).generate(schema, path)
      }
      .onRight { file -> echoSuccess("$file generated successfully.") }
      .onLeft(::echoProblemsAndExit)
  }

  private fun createGenerator(formatType: FormatType, platform: Platform): Generator {
    return when (formatType) {
      FormatType.Kotlin -> KotlinGenerator(namespace, platform)
      FormatType.Swift -> SwiftGenerator(namespace, platform)
      FormatType.TypeScript -> TypeScriptGenerator(platform)
      FormatType.Json -> JsonGenerator()
    }
  }

  private fun requireCorrectOutput(outputDir: Path, format: FormatType) {
    when (format) {
      FormatType.Kotlin, FormatType.Swift, FormatType.TypeScript -> {
        if (!outputDir.isDirectory()) {
          throw UsageError("--output-path option must be a file in combination with $format format")
        }
      }
      FormatType.Json -> Unit
    }
  }

  private fun requireDeclaredPlatform(schema: Schema, format: FormatType): Pair<Schema, Platform> {
    val platform = if (format != FormatType.Json && schema.platforms.isNotEmpty()) {
      val platform = requireOption(outputPlatform) { "missing option --output-platform" }
      if (schema.platforms.isNotEmpty() && platform !in schema.platforms) {
        val platformValues = schema.platforms.map(Platform::value)
        throw UsageError(
          "Invalid value for --output-platform: ${platform.value}. Must be one of the schema-declared values: $platformValues",
        )
      }
      platform
    } else {
      NoPlatform
    }
    return schema to platform
  }

  private fun echoSuccess(message: String) {
    echo(currentContext.theme.success(message))
  }

  private fun echoProblemsAndExit(problems: List<Problem>) {
    val message = problems.joinToString(separator = "\n\n") { problem -> problem.print() }
    echo(message, err = true)
    throw ProgramResult(1)
  }
}

private val NoPlatform = Platform("")

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
