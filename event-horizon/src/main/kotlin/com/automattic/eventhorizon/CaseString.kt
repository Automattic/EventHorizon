package com.automattic.eventhorizon

@ConsistentCopyVisibility
public data class CaseString private constructor(
  val rawValue: String,
  val case: Case,
) {
  public fun toString(case: Case): String {
    val tokens = this.case.tokenize(rawValue)
    return case.joinToString(tokens)
  }

  public companion object {
    public fun String.toCaseString(): CaseString = CaseString(this, Case.detectCase(this))
  }
}

public enum class Case(
  internal val wordSeparator: Char?,
) {
  Camel(
    wordSeparator = null,
  ),
  Pascal(
    wordSeparator = null,
  ),
  Snake(
    wordSeparator = '_',
  ),
  Kebab(
    wordSeparator = '-',
  ),
  Dot(
    wordSeparator = '.',
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

  internal companion object {
    fun detectCase(string: String): Case {
      val possibleCases = Case.entries.toMutableSet()
      string.forEachIndexed { index, char ->
        possibleCases.removeIf { case -> !case.isCharAllowed(index, char) }
      }
      return when {
        Camel in possibleCases -> Camel
        Pascal in possibleCases -> Pascal
        else -> requireNotNull(possibleCases.singleOrNull()) {
          buildString {
            append("Failed to detect case of '")
            append(string)
            append("' string. Supported cases:")
            append(Case.entries.joinToString(prefix = "\n", separator = "\n") { case -> " - $case" })
          }
        }
      }
    }
  }
}
