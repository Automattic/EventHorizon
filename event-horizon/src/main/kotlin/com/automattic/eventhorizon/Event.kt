package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.automattic.eventhorizon.utils.ensureNoDuplicatesBy

@ConsistentCopyVisibility
public data class Event private constructor(
  val name: CaseString,
  val properties: List<Property>,
  val description: String?,
  val excludedPlatforms: Set<Platform>,
) {
  public companion object {
    public operator fun invoke(
      name: String,
      properties: List<Property>,
      description: String?,
      excludedPlatforms: Set<Platform>,
    ): Either<EventProblem, Event> = either {
      val caseName = ensureValidName(name)
      ensureValidProperties(name, properties)

      Event(caseName, properties, description, excludedPlatforms)
    }
  }
}

public sealed interface EventProblem : Problem {
  public data object BlankName : EventProblem {
    override fun print(): String {
      return "Event cannot have a blank name"
    }
  }

  public data class UnknownNameCase(val name: String) : EventProblem {
    override fun print(): String {
      return "Event '$name' uses unsupported naming convention. ${Case.supportedConventionsMessage}"
    }
  }

  public data class DuplicateProperties(val eventName: String, val duplicates: Map<String, Int>) : EventProblem {
    override fun print(): String {
      return "Event '$eventName' has duplicate properties: $duplicates"
    }
  }
}

private fun Raise<EventProblem>.ensureValidName(name: String): CaseString {
  ensure(name.isNotBlank()) { EventProblem.BlankName }
  return CaseString(name).mapLeft(EventProblem::UnknownNameCase).bind()
}

private fun Raise<EventProblem>.ensureValidProperties(eventName: String, properties: List<Property>) {
  ensureNoDuplicatesBy(properties, Property::uniqueKey) { duplicates ->
    EventProblem.DuplicateProperties(eventName, duplicates)
  }
}

private val Property.uniqueKey get() = name.rawValue
