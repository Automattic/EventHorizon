package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.enumType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventPropertyEnumSpec : FunSpec({
  test("enum type") {
    val enum = enumType("enum_name", "value_1", "value_2")

    val typeSpec = EventPropertyEnum("MyModule", enum, AnalyticsValueProtocol("MyModule")).typeSpec

    typeSpec.toString() shouldBe """
      |public enum EnumName : Swift.String, MyModule.AnalyticsValue {
      |
      |  case value1 = "value_1"
      |  case value2 = "value_2"
      |
      |}
      |
    """.trimMargin()
  }
})
