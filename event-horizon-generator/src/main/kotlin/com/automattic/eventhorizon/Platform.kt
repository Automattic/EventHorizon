package com.automattic.eventhorizon

@JvmInline
public value class Platform(
  public val value: String,
) {
  public companion object {
    public val NoPlatform: Platform = Platform("")
  }
}
