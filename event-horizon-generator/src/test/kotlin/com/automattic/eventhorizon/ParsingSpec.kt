package com.automattic.eventhorizon

import com.automattic.eventhorizon.Property.Type
import com.charleskorn.kaml.Location
import com.charleskorn.kaml.MissingRequiredPropertyException
import com.charleskorn.kaml.YamlException
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import kotlin.io.path.writeText

class ParsingSpec : FunSpec({
  val tempFile = tempfile().toPath()

  test("parse empty file") {
    tempFile.writeText("")

    val result = parseSchema(tempFile)

    val value = result.shouldBeSuccess()
    value shouldBe EventHorizonSchema.Empty
  }

  test("parse schema version") {
    val text = """
      |version: 1
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val value = result.shouldBeSuccess()
    value.schemaVersion shouldBe 1u
  }

  test("parse negative schema version") {
    val text = """
      |version: -1
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val exception = result.shouldBeFailure<IllegalArgumentException>()
    exception shouldHaveMessage "Schema version must be a positive number. Is: -1"
  }

  test("parse non-numeric schema version") {
    val text = """
      |version: some text
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val exception = result.shouldBeFailure<IllegalArgumentException>()
    exception shouldHaveMessage "Schema version must be a positive number. Is: some text"
  }

  test("parse event without properties") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event shouldBe Event("event")
  }

  test("parse events without schema") {
    val text = """
      |events:
      |  event:
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val exception = result.shouldBeFailure<MissingRequiredPropertyException>()
    exception shouldHaveMessage "Property 'version' is required but it is missing."
  }

  test("parse event with text property") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property:
      |      type: text
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Text
  }

  test("parse event with number property") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property:
      |      type: number
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Number
  }

  test("parse event with boolean property") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property:
      |      type: boolean
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Boolean
  }

  test("parse event with enum property that exists") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property:
      |      type: enum_reference
      |enums:
      |  enum_reference:
      |    - value_1
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Enum("enum_reference", "value_1")
  }

  test("parse event with enum property that doesn't exist") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property:
      |      type: enum_reference
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception shouldHaveMessage "Value 'enum_reference' must be one of boolean, number, text, or a predefined enum."
    exception.location shouldBe Location(line = 6, column = 13)
  }

  test("parse enum values") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property:
      |      type: enum_reference
      |enums:
      |  enum_reference:
      |    - value1
      |    - value2
      |    - value3
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Enum("enum_reference", "value1", "value2", "value3")
  }

  test("parse event with optional property on all platforms") {
    val text = """
      |version: 1
      |platforms:
      |  - android
      |  - ios
      |
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional: true
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldContainExactly setOf(Platform("android"), Platform("ios"))
  }

  test("parse event with non-optional property") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional: false
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms.shouldBeEmpty()
  }

  test("parse event with optional property on a single platform") {
    val text = """
      |version: 1
      |platforms:
      |  - android
      |  - ios
      |
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional:
      |        - android
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldHaveSingleElement Platform("android")
  }

  test("parse event with optional property on multiple platforms") {
    val text = """
      |version: 1
      |platforms:
      |  - android
      |  - ios
      |  - web
      |
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional:
      |        - android
      |        - ios
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldBe setOf(Platform("android"), Platform("ios"))
  }

  test("parse event with multiple properties") {
    val text = """
      |version: 1
      |platforms:
      |  - android
      |  - web
      |
      |events:
      |  event:
      |    property1:
      |      type: text
      |      optional: true
      |    property2:
      |      type: boolean
      |      optional:
      |        - android
      |    property3:
      |      type: number
      |    property4:
      |      type: number
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event.properties shouldBe
      setOf(
        Property("property1", "android", "web", type = Type.Text),
        Property("property2", "android", type = Type.Boolean),
        Property("property3", type = Type.Number),
        Property("property4", type = Type.Number),
      )
  }

  test("parse multiple events") {
    val text = """
      |version: 1
      |
      |events:
      |  event1:
      |  event2:
      |  event3:
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val events = result.shouldBeSuccess().events
    events shouldBe Events(Event("event1"), Event("event2"), Event("event3"))
  }

  test("parse event description") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    description: Some description
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event shouldBe Event("event", description = "Some description")
  }

  test("parse event with description set as one of properties") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    description:
      |      type: text
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception shouldHaveMessage "'description' cannot be used as a property name"
    exception.location shouldBe Location(line = 6, column = 7)
  }

  test("parse property description") {
    val text = """
      |version: 1
      |
      |events:
      |  event:
      |    property1:
      |      type: text
      |      description: Property description
    """
    tempFile.writeText(text.trimMargin())

    val result = parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement Property("property1", description = "Property description")
  }
})

@Suppress("NOTHING_TO_INLINE")
private inline fun <T> Collection<T>.shouldHaveSingleElement(): T {
  shouldBeSingleton()
  return first()
}
