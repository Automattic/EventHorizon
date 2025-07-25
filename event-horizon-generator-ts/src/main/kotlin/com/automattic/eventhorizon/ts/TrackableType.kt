package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type

internal class TrackableType(
  private val events: Events,
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
    event.description?.let { description ->
      appendIndent()
      append("// ")
      append(description)
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
    property.description?.let { description ->
      appendNewLine()
      appendIndent(count = 4)
      append("// ")
      append(description)
    }
  }

  private fun StringBuilder.appendProperty(property: Property) {
    appendNewLine()
    appendIndent(count = 4)
    append(property.name)
    if (property.isOptional(Platform.Web)) {
      append('?')
    }
    append(": ")
    append(property.typeName)
    append(';')
  }

  private val Property.typeName
    get() = when (val type = type) {
      is Type.Boolean -> "boolean"
      is Type.Number -> "number"
      is Type.Text -> "string"
      is Type.Enum -> EventPropertyType(type).typeName
    }
}
