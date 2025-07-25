package com.automattic.eventhorizon.swift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventHorizonClassSpec : FunSpec({
  test("trackable protocol") {
    val typeSpec = EventHorizonClass("MyModule", TrackableProtocol("MyModule")).typeSpec

    typeSpec.toString() shouldBe """
      |class EventHorizon {
      |
      |  private let eventSink: (Swift.String, [Swift.AnyHashable : Swift.Any]) -> Swift.Void
      |
      |  init(eventSink: @escaping (Swift.String, [Swift.AnyHashable : Swift.Any]) -> Swift.Void) {
      |    self.eventSink = eventSink
      |  }
      |
      |  func track(_ event: MyModule.Trackable) {
      |    eventSink(event.trackableName, event.trackableProperties)
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
