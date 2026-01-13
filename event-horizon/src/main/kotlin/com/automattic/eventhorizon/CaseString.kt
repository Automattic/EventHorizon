package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.EitherNel
import arrow.core.Option
import arrow.core.mapOrAccumulate
import arrow.core.toOption

@ConsistentCopyVisibility
public data class CaseString private constructor(
  val rawValue: String,
  val case: Case,
) {
  public fun toString(case: Case): String {
    val tokens = this.case.tokenize(rawValue)
    return case.joinToString(tokens)
  }

  public fun toHumanReadableString(uppercaseWords: Boolean = false): String {
    val tokens = case.tokenize(rawValue)
    return buildString {
      tokens.forEachIndexed { index, token ->
        if (uppercaseWords) {
          append(token.replaceFirstChar(Char::uppercase))
        } else {
          append(token)
        }
        if (index != tokens.lastIndex) {
          append(' ')
        }
      }
    }
  }

  public companion object {
    public operator fun invoke(value: String): Either<String, CaseString> {
      return Case
        .detectCase(value)
        .map { case -> CaseString(value, case) }
        .toEither { value }
    }

    public fun fromAll(values: Iterable<String>): EitherNel<String, List<CaseString>> {
      return values.mapOrAccumulate { value -> CaseString(value).bind() }
    }
  }
}

public enum class Case(
  internal val wordSeparator: Char?,
  private val supportedChars: List<String>,
) {
  Camel(
    wordSeparator = null,
    supportedChars = listOf("alphanumeric"),
  ),
  Pascal(
    wordSeparator = null,
    supportedChars = listOf("alphanumeric"),
  ),
  Snake(
    wordSeparator = '_',
    supportedChars = listOf("alphanumeric", "'_'"),
  ),
  Kebab(
    wordSeparator = '-',
    supportedChars = listOf("alphanumeric", "'-'"),
  ),
  Dot(
    wordSeparator = '.',
    supportedChars = listOf("alphanumeric", "'.'"),
  ),
  ;

  internal fun tokenize(string: String): List<String> {
    return if (wordSeparator != null) {
      string.lowercase().split(wordSeparator)
    } else {
      buildList {
        val wordBuilder = StringBuilder()

        string.forEachIndexed { index, char ->
          if (char.isUpperCase() && wordBuilder.isNotEmpty()) {
            add(wordBuilder.toString())
            wordBuilder.clear()
          }

          wordBuilder.append(char.lowercase())

          if (index == string.lastIndex) {
            add(wordBuilder.toString())
          }
        }
      }
    }
  }

  internal fun joinToString(tokens: List<String>): String {
    return buildString {
      tokens.forEachIndexed { index, string ->
        append(tokenToString(index, string))
        if (wordSeparator != null && index != tokens.lastIndex) {
          append(wordSeparator)
        }
      }
    }
  }

  private fun tokenToString(index: Int, token: String): String {
    return when (this) {
      Snake, Kebab, Dot -> token

      Camel -> if (index == 0) {
        token
      } else {
        token.replaceFirstChar(Char::uppercase)
      }

      Pascal -> token.replaceFirstChar(Char::uppercase)
    }
  }

  private fun isCharAllowed(index: Int, char: Char): Boolean {
    return when (this) {
      Snake, Kebab, Dot -> char == wordSeparator || char.isLetterOrDigit()

      Camel -> if (index == 0) {
        char.isLetterOrDigit() && char.isLowerCase()
      } else {
        char.isLetterOrDigit()
      }

      Pascal -> char.isLetterOrDigit()
    }
  }

  public companion object {
    public val supportedConventionsMessage: String = buildString {
      append("Supported conventions:\n")
      append(entries.joinToString(separator = "\n") { case -> " - $case: ${case.supportedChars}" })
    }

    internal fun detectCase(string: String): Option<Case> {
      val possibleCases = entries.toMutableSet()
      string.forEachIndexed { index, char ->
        possibleCases.removeIf { case -> !case.isCharAllowed(index, char) }
      }
      return when {
        Camel in possibleCases -> Camel
        Pascal in possibleCases -> Pascal
        else -> possibleCases.singleOrNull()
      }.toOption()
    }
  }
}
