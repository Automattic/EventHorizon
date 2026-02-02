package com.automattic.eventhorizon.swift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TrackableProtocolSpec : FunSpec({
  test("trackable protocol") {
    val typeSpec = TrackableProtocol("MyModule").typeSpec

    typeSpec.toString() shouldBe """
      |public protocol Trackable {
      |
      |  var name: Swift.String { get }
      |  var properties: [Swift.AnyHashable : Swift.Any] { get }
      |
      |}
      |
    """.trimMargin()
  }
})
