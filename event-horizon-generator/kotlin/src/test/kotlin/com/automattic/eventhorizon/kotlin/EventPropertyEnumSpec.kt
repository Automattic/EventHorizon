package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.enumType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventPropertyEnumSpec : FunSpec({
  test("enum type") {
    val enum = enumType("enum_name", "value_1", "value_2")

    val typeSpec = EventPropertyEnum("dev.sample", enum).typeSpec

    typeSpec.toString() shouldBe """
      |public enum class EnumName {
      |  Value1 {
      |    override fun toString(): kotlin.String = "value_1"
      |  },
      |  Value2 {
      |    override fun toString(): kotlin.String = "value_2"
      |  },
      |}
      |
    """.trimMargin()
  }
})
