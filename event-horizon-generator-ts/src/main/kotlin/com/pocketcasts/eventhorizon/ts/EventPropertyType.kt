package com.pocketcasts.eventhorizon.ts

import com.pocketcasts.eventhorizon.Property.Type
import com.pocketcasts.eventhorizon.snakeToPascalCase

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
