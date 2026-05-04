package com.automattic.eventhorizon.swift

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AnalyticsValueProtocolSpec : FunSpec({
  val protocol = AnalyticsValueProtocol("MyModule")

  test("analytics value protocol") {
    protocol.typeSpec.toString() shouldBe """
      |public protocol AnalyticsValue {
      |
      |  var analyticsValue: Swift.String { get }
      |
      |}
      |
    """.trimMargin()
  }

  test("analytics value extension") {
    protocol.extensionSpec.toString() shouldBe """
      |extension MyModule.AnalyticsValue where Self : Swift.RawRepresentable,
      |    Self.RawValue == Swift.String {
      |
      |  public var analyticsValue: Swift.String {
      |    rawValue
      |  }
      |}
      |
    """.trimMargin()
  }
})
