package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.PropertyType

internal class TrackableType(
  private val events: Events,
  private val platform: Platform,
) {
  val typeSpec
    get() = buildString {
      append("export type Trackable = {")
      appendNewLine()
      appendEvents()
      append("};")
    }

  private fun StringBuilder.appendEvents() {
    events.forEachIndexed { index, event ->
      appendEventComment(event)
      appendEventName(event)
      if (event.properties.isEmpty()) {
        append("undefined;")
      } else {
        appendEventProperties(event)
      }
      appendNewLine(count = if (index == events.lastIndex) 1 else 2)
    }
  }

  private fun StringBuilder.appendEventComment(event: Event) {
    event.documentation?.let { documentation ->
      appendIndent()
      append("// ")
      append(documentation)
      appendNewLine()
    }
  }

  private fun StringBuilder.appendEventName(event: Event) {
    appendIndent()
    append('"')
    append(event.name)
    append("\": ")
  }

  private fun StringBuilder.appendEventProperties(event: Event) {
    append("{")
    event.properties.forEachIndexed { index, property ->
      appendPropertyComment(property)
      appendProperty(property)
      if (index == event.properties.lastIndex) {
        appendNewLine()
        appendIndent()
        append("};")
      }
    }
  }

  private fun StringBuilder.appendPropertyComment(property: Property) {
    property.documentation?.let { documentation ->
      appendNewLine()
      appendIndent(count = 4)
      append("// ")
      append(documentation)
    }
  }

  private fun StringBuilder.appendProperty(property: Property) {
    appendNewLine()
    appendIndent(count = 4)
    append(property.name)
    if (property.isOptional(platform)) {
      append('?')
    }
    append(": ")
    append(property.typeName)
    append(';')
  }

  private val Property.typeName
    get() = when (val type = type) {
      is PropertyType.Boolean -> "boolean"
      is PropertyType.Number -> "number"
      is PropertyType.Text -> "string"
      is PropertyType.Enum -> EventPropertyType(type).typeName
    }
}
