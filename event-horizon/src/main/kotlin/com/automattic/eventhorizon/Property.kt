package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.automattic.eventhorizon.utils.ensureNotBlank

@ConsistentCopyVisibility
public data class Property private constructor(
  val name: CaseString,
  val type: PropertyType,
  val description: String?,
  val optionalPlatforms: Set<Platform>,
) {
  public fun isOptional(platform: Platform): Boolean = platform in optionalPlatforms

  public companion object {
    public operator fun invoke(
      name: String,
      type: PropertyType,
      description: String?,
      optionalPlatforms: Set<Platform>,
    ): Either<PropertyProblem, Property> = either {
      val caseName = ensureValidName(name)

      Property(caseName, type, description, optionalPlatforms)
    }
  }
}

public sealed interface PropertyProblem : Problem {
  public data object BlankName : PropertyProblem {
    override fun print(): String {
      return "Property cannot have a blank name"
    }
  }

  public data class UnknownNameCase(val name: String) : PropertyProblem {
    override fun print(): String {
      return "Property '$name' uses unsupported naming convention. ${Case.SupportedConventionsMessage}"
    }
  }
}

private fun Raise<PropertyProblem>.ensureValidName(name: String): CaseString {
  ensureNotBlank(name) { PropertyProblem.BlankName }
  return CaseString(name).mapLeft(PropertyProblem::UnknownNameCase).bind()
}
