package com.automattic.eventhorizon

public sealed interface Problem {
  public fun print(): String
}

public class SimpleProblem(
  private val message: String,
) : Problem {
  override fun print(): String = message

  override fun toString(): String = "SimpleProblem($message)"

  override fun equals(other: Any?): Boolean = other === this || (other is SimpleProblem && other.message == message)

  override fun hashCode(): Int = message.hashCode()
}

public class ErrorProblem(
  public val error: Throwable,
  private val message: (Throwable) -> String = DefaultProblemMessage,
) : Problem {
  override fun print(): String {
    return message(error)
  }

  override fun toString(): String = "ErrorProblem($error)"

  override fun equals(other: Any?): Boolean = other === this || (other is ErrorProblem && other.error == error)

  override fun hashCode(): Int = error.hashCode()
}

private val DefaultProblemMessage = { error: Throwable -> error.message ?: "Failure: $error" }
