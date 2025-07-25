package com.automattic.eventhorizon.kotlin

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventHorizonClassSpec : FunSpec({
  test("trackable interface") {
    val typeSpec = EventHorizonClass("dev.sample", TrackableInterface("dev.sample")).typeSpec

    typeSpec.toString() shouldBe """
      |public class EventHorizon(
      |  private val eventSink: (kotlin.String, kotlin.collections.Map<kotlin.String, kotlin.Any>) -> kotlin.Unit,
      |) {
      |  public fun track(event: dev.sample.Trackable) {
      |    eventSink(event.trackableName, event.trackableProperties)
      |  }
      |}
      |
    """.trimMargin()
  }
})
