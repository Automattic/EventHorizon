package com.automattic.eventhorizon.swift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventStructSpec : FunSpec({
  test("event struct") {
    val typeSpec = EventStruct("MyModule").typeSpec

    typeSpec.toString() shouldBe """
      |public struct Event {
      |
      |  public let name: Swift.String
      |  public let properties: [Swift.String : Swift.CustomStringConvertible]
      |
      |}
      |
    """.trimMargin()
  }
})
