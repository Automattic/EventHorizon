package com.automattic.eventhorizon

import arrow.core.NonEmptySet

public sealed interface PropertyType {
  public sealed interface Basic : PropertyType

  public data object Text : Basic

  public data object Number : Basic

  public data object Boolean : Basic

  public data class Enum(
    val name: String,
    val values: NonEmptySet<String>,
  ) : PropertyType
}
