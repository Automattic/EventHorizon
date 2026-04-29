package com.automattic.eventhorizon.swift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventHorizonClassSpec : FunSpec({
  test("event horizon class") {
    val typeSpec = EventHorizonClass("MyModule", EventStruct("MyModule")).typeSpec

    typeSpec.toString() shouldBe """
      |public class EventHorizon {
      |
      |  private let eventSink: (MyModule.Event) -> Swift.Void
      |
      |  public init(eventSink: @escaping (MyModule.Event) -> Swift.Void) {
      |    self.eventSink = eventSink
      |  }
      |
      |  public func track(_ event: MyModule.Event) {
      |    eventSink(event)
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
