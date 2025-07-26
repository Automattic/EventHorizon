package com.automattic.eventhorizon.swift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TrackableProtocolSpec : FunSpec({
  test("trackable protocol") {
    val typeSpec = TrackableProtocol("MyModule").typeSpec

    typeSpec.toString() shouldBe """
      |protocol Trackable {
      |
      |  var trackableName: Swift.String { get }
      |  var trackableProperties: [Swift.AnyHashable : Swift.Any] { get }
      |
      |}
      |
    """.trimMargin()
  }
})
