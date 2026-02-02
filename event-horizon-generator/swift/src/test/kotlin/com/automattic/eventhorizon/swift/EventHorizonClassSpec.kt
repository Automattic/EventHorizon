package com.automattic.eventhorizon.swift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventHorizonClassSpec : FunSpec({
  test("trackable protocol") {
    val typeSpec = EventHorizonClass("MyModule", TrackableProtocol("MyModule")).typeSpec

    typeSpec.toString() shouldBe """
      |public class EventHorizon {
      |
      |  private let eventSink: (MyModule.Trackable) -> Swift.Void
      |
      |  public init(eventSink: @escaping (MyModule.Trackable) -> Swift.Void) {
      |    self.eventSink = eventSink
      |  }
      |
      |  public func track(_ event: MyModule.Trackable) {
      |    eventSink(event)
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
