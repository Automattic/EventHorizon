package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.Nel
import arrow.core.NonEmptySet
import arrow.core.mapOrAccumulate
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptySetOrNull
import com.automattic.eventhorizon.utils.ensureNotBlank
import com.automattic.eventhorizon.utils.unit

public sealed interface PropertyType {
  public sealed interface Basic : PropertyType

  public data object Text : Basic

  public data object Number : Basic

  public data object Boolean : Basic

  @ConsistentCopyVisibility
  public data class Enum private constructor(
    val name: CaseString,
    val values: NonEmptySet<CaseString>,
  ) : PropertyType {
    public companion object {
      public operator fun invoke(name: String, values: Set<String>): Either<EnumTypeProblem, Enum> = either {
        val caseName = ensureValidName(name)
        val caseValues = ensureValidValues(name, values)

        Enum(caseName, caseValues)
      }
    }
  }
}

public sealed interface EnumTypeProblem : Problem {
  public data object BlankName : EnumTypeProblem {
    override fun print(): String {
      return "Enum cannot have a blank name"
    }
  }

  public data class UnknownNameCase(val name: String) : EnumTypeProblem {
    override fun print(): String {
      return "Enum '$name' uses unsupported naming convention. ${Case.supportedConventionsMessage}"
    }
  }

  public data class NoValues(val enumName: String) : EnumTypeProblem {
    override fun print(): String {
      return "Enum '$enumName' has no values"
    }
  }

  public data class BlankValues(val enumName: String) : EnumTypeProblem {
    override fun print(): String {
      return "Enum '$enumName' has blank values"
    }
  }

  public data class UnknownValueCases(val enumName: String, val values: Nel<String>) : EnumTypeProblem {
    override fun print(): String {
      return "Enum '$enumName' has values with unsupported naming conventions: $values. ${Case.supportedConventionsMessage}"
    }
  }
}

private fun Raise<EnumTypeProblem>.ensureValidName(name: String): CaseString {
  ensureNotBlank(name) { EnumTypeProblem.BlankName }
  return CaseString(name).mapLeft(EnumTypeProblem::UnknownNameCase).bind()
}

private fun Raise<EnumTypeProblem>.ensureValidValues(enumName: String, values: Set<String>): NonEmptySet<CaseString> {
  ensureNoBlankValues(enumName, values)
  val caseValues = ensureCaseStringValues(enumName, values)
  return ensureNotNull(caseValues.toNonEmptySetOrNull()) { EnumTypeProblem.NoValues(enumName) }
}

private fun Raise<EnumTypeProblem>.ensureNoBlankValues(enumName: String, values: Set<String>) {
  values
    .mapOrAccumulate { value -> ensureNotBlank(value, ::unit) }
    .mapLeft { EnumTypeProblem.BlankValues(enumName) }
    .bind()
}

private fun Raise<EnumTypeProblem>.ensureCaseStringValues(enumName: String, values: Set<String>): List<CaseString> {
  return CaseString
    .fromAll(values)
    .mapLeft { invalidValues -> EnumTypeProblem.UnknownValueCases(enumName, invalidValues) }
    .bind()
}
