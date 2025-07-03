package com.pocketcasts.eventhorizon

import com.charleskorn.kaml.Location
import com.charleskorn.kaml.YamlException
import com.pocketcasts.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldBeSingleton
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import kotlin.io.path.writeText

class ParsingSpec : FunSpec({
  val tempFile = tempfile().toPath()

  test("parse empty file") {
    tempFile.writeText("")

    val result = parseEvents(tempFile)

    val events = result.shouldBeSuccess()
    events.shouldBeEmpty()
  }

  test("parse event without properties") {
    val text = """
      |events:
      |  event:
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    event shouldBe Event("event")
  }

  test("parse event with text property") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Text
  }

  test("parse event with number property") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: number
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Number
  }

  test("parse event with boolean property") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: boolean
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Boolean
  }

  test("parse event with enum property that exists") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: enum_reference
      |enums:
      |  enum_reference:
      |    - value_1
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Enum("enum_reference", "value_1")
  }

  test("parse event with enum property that doesn't exist") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: enum_reference
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception.message shouldBe "Value 'enum_reference' must be one of boolean, number, text, or a predefined enum."
    exception.location shouldBe Location(line = 4, column = 13)
  }

  test("parse enum values") {
    val text = """
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

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe Type.Enum("enum_reference", "value1", "value2", "value3")
  }

  test("parse event with optional property") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional: true
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldContainExactly Platform.entries
  }

  test("parse event with non-optional property") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional: false
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms.shouldBeEmpty()
  }

  test("parse event with optional property on android platform") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional:
      |        - android
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldHaveSingleElement Platform.Android
  }

  test("parse event with optional property on ios platform") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional:
      |        - ios
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldHaveSingleElement Platform.Ios
  }

  test("parse event with optional property on web platform") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional:
      |        - web
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldHaveSingleElement Platform.Web
  }

  test("parse event with optional property on multiple platforms") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional:
      |        - android
      |        - ios
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldBe setOf(Platform.Android, Platform.Ios)
  }

  test("parse event with optional property on unknown platform") {
    val text = """
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional:
      |        - unknown
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception.message shouldBe "Value 'unknown' must be one of android, ios, or web."
    exception.location shouldBe Location(line = 6, column = 9)
  }

  test("parse event with multiple properties") {
    val text = """
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

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    event.properties shouldBe
      setOf(
        Property("property1", Type.Text, optAndroid = true, optIos = true, optWeb = true),
        Property("property2", Type.Boolean, optAndroid = true),
        Property("property3", Type.Number),
        Property("property4", Type.Number),
      )
  }

  test("parse multiple events") {
    val text = """
      |events:
      |  event1:
      |  event2:
      |  event3:
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val events = result.shouldBeSuccess()
    events shouldBe Events(Event("event1"), Event("event2"), Event("event3"))
  }

  test("parse event description") {
    val text = """
      |events:
      |  event:
      |    description: Some description
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    event shouldBe Event("event", description = "Some description")
  }

  test("parse event with description set as one of properties") {
    val text = """
      |events:
      |  event:
      |    description:
      |      type: text
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception.message shouldBe "'description' cannot be used as a property name"
    exception.location shouldBe Location(line = 4, column = 7)
  }

  test("parse property description") {
    val text = """
      |events:
      |  event:
      |    property1:
      |      type: text
      |      description: Property description
    """
    tempFile.writeText(text.trimMargin())

    val result = parseEvents(tempFile)

    val event = result.shouldBeSuccess().shouldHaveSingleElement()
    event.properties shouldHaveSingleElement Property("property1", description = "Property description")
  }
})

@Suppress("NOTHING_TO_INLINE")
private inline fun <T> Collection<T>.shouldHaveSingleElement(): T {
  shouldBeSingleton()
  return first()
}
