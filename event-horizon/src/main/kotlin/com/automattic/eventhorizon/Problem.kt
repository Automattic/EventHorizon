package com.automattic.eventhorizon

public sealed interface Problem {
  public fun print(): String
}

public class GenericProblem(
  public val error: Throwable,
  private val message: (Throwable) -> String = DefaultProblemMessage,
) : Problem {
  override fun print(): String {
    return message(error)
  }

  override fun toString(): String = "GenericProblem($error)"

  override fun equals(other: Any?): Boolean = other === this || (other is GenericProblem && other.error == error)

  override fun hashCode(): Int = error.hashCode()
}

private val DefaultProblemMessage = { error: Throwable -> error.message ?: "Failure: $error" }
