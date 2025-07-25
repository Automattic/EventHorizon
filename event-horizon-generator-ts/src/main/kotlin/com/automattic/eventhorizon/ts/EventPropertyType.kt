package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Property.Type
import com.automattic.eventhorizon.snakeToPascalCase

internal class EventPropertyType(
  private val enum: Type.Enum,
) {
  val typeName
    get() = enum.name.snakeToPascalCase()

  val typeSpec
    get() = buildString {
      append("export type ")
      append(typeName)
      append(" =")
      enum.values.forEachIndexed { index, value ->
        appendNewLine()
        appendIndent(count = 4)
        append("| ")
        append('"')
        append(value)
        append('"')
      }
      append(";")
    }
}
