package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.enumType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventPropertyTypeSpec : FunSpec({
  test("enum type") {
    val enum = enumType("enum_name", "value_1", "value_2")

    val typeSpec = EventPropertyType(enum).typeSpec

    typeSpec shouldBe """
      |export type EnumName =
      |    | "value_1"
      |    | "value_2";
    """.trimMargin()
  }
})
