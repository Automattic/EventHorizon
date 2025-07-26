package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Case
import com.automattic.eventhorizon.PropertyType

internal class EventPropertyType(
  private val enum: PropertyType.Enum,
) {
  val typeName
    get() = enum.name.toString(Case.Pascal)

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
