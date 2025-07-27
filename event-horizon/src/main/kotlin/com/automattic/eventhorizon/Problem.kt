package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.getOrElse

public sealed interface Problem {
  public fun print(): String
}

public fun <L, R> Either<L, R>.require(): R = getOrElse { throw IllegalArgumentException(it.toString()) }
