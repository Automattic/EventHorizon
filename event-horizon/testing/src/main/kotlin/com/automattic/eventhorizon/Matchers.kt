package com.automattic.eventhorizon

import io.kotest.matchers.collections.shouldBeSingleton

public fun <T> Collection<T>.shouldHaveSingleElement(): T {
  shouldBeSingleton()
  return first()
}
