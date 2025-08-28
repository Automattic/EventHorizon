package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure

@ConsistentCopyVisibility
public data class Group private constructor(
  val key: CaseString,
  val name: String,
  val description: String?,
) {
  public companion object {
    public val empty: Group = invoke(
      key = "ungrouped",
      name = null,
      description = null,
    ).getOrElse { throw AssertionError("This should never happen") }

    public operator fun invoke(key: String, name: String?, description: String?): Either<GroupProblem, Group> = either {
      val caseKey = ensureValidKey(key)
      val name = name ?: caseKey.toHumanReadableString(uppercaseWords = false).replaceFirstChar(Char::uppercase)

      Group(caseKey, name, description)
    }
  }
}

public sealed interface GroupProblem : Problem {
  public data object BlankKey : GroupProblem {
    override fun print(): String {
      return "Group cannot have a blank key"
    }
  }

  public data class UnknownKeyCase(val key: String) : GroupProblem {
    override fun print(): String {
      return "Group '$key' uses unsupported naming convention. ${Case.supportedConventionsMessage}"
    }
  }
}

private fun Raise<GroupProblem>.ensureValidKey(key: String): CaseString {
  ensure(key.isNotBlank()) { GroupProblem.BlankKey }
  return CaseString(key).mapLeft(GroupProblem::UnknownKeyCase).bind()
}
