package com.automattic.eventhorizon

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

class YamlParserSpec : FunSpec({
  val parser = YamlParser()
  val tempFile = tempfile().toPath()

  test("parse an empty file") {
    tempFile.writeText("")

    val result = parser.parseSchema(tempFile)

    val value = result.shouldBeSuccess()
    value shouldBe Schema.Empty
  }

  test("parse a blank file") {
    tempFile.writeText(" \n ")

    val result = parser.parseSchema(tempFile)

    val value = result.shouldBeSuccess()
    value shouldBe Schema.Empty
  }

  test("parse a schema version") {
    val text = """
      |schemaVersion: 1
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val value = result.shouldBeSuccess()
    value.version shouldBe 1u
  }

  test("fail to parse a negative schema version") {
    val text = """
      |schemaVersion: -1
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val exception = result.shouldBeFailure<IllegalArgumentException>()
    exception shouldHaveMessage "Value for 'schemaVersion' must be a number between 1 and 18446744073709551615. Found: '-1'"
  }

  test("fail to parse a non-numeric schema version") {
    val text = """
      |schemaVersion: foo
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val exception = result.shouldBeFailure<IllegalArgumentException>()
    exception shouldHaveMessage "Value for 'schemaVersion' must be a number between 1 and 18446744073709551615. Found: 'foo'"
  }

  test("fail to parse events without a schema version") {
    val text = """
      |events:
      |  event:
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val exception = result.shouldBeFailure<MissingRequiredPropertyException>()
    exception shouldHaveMessage "Property 'schemaVersion' is required but it is missing."
  }

  test("parse an event without properties") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event shouldBe buildEvent("event")
  }

  test("parse an event with a text property") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property:
      |      type: text
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe PropertyType.Text
  }

  test("parse an event with a number property") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property:
      |      type: number
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe PropertyType.Number
  }

  test("parse event with a boolean property") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property:
      |      type: boolean
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe PropertyType.Boolean
  }

  test("parse an event with an enum property that exists") {
    val text = """
      |schemaVersion: 1
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

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe enumType("enum_reference", "value_1")
  }

  test("fail to parse an event with an enum property that doesn't exist") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property:
      |      type: enum_reference
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception shouldHaveMessage "Value 'enum_reference' must be one of 'boolean', 'number', 'text', or a predefined enum."
    exception.location shouldBe Location(line = 6, column = 13)
  }

  test("parse enum values") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property:
      |      type: enum_reference
      |enums:
      |  enum_reference:
      |    - value1
      |    - value2
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.type shouldBe enumType("enum_reference", "value1", "value2")
  }

  test("parse an event with an optional property on all platforms") {
    val text = """
      |schemaVersion: 1
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

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldContainExactly setOf(Platform("android"), Platform("ios"))
  }

  test("parse an event with a non-optional property") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional: false
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms.shouldBeEmpty()
  }

  test("parse an event with an optional property on a single platform") {
    val text = """
      |schemaVersion: 1
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

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldHaveSingleElement Platform("android")
  }

  test("parse an event with an optional property on multiple platforms") {
    val text = """
      |schemaVersion: 1
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

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    val property = event.properties.shouldHaveSingleElement()
    property.optionalPlatforms shouldBe setOf(Platform("android"), Platform("ios"))
  }

  test("fail to parse an event with a null optional property") {
    val text = """
      |schemaVersion: 1
      |platforms:
      |  - android
      |  - ios
      |  - web
      |
      |events:
      |  event:
      |    property:
      |      type: text
      |      optional: null
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception shouldHaveMessage "Expected element to be YamlScalar or YamlList but is YamlNull"
    exception.location shouldBe Location(line = 11, column = 17)
  }

  test("fail to parse an event with a map optional property") {
    val text = """
      |schemaVersion: 1
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
      |        key: value
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception shouldHaveMessage "Expected element to be YamlScalar or YamlList but is YamlMap"
    exception.location shouldBe Location(line = 12, column = 9)
  }

  test("parse an event with multiple properties") {
    val text = """
      |schemaVersion: 1
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
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event.properties shouldBe buildProperties {
      text("property1") {
        optionalPlatforms("android", "web")
      }
      boolean("property2") {
        optionalPlatforms("android")
      }
      number("property3")
    }
  }

  test("parse multiple events") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event1:
      |  event2:
      |  event3:
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val events = result.shouldBeSuccess().events
    events shouldBe buildEvents {
      event("event1")
      event("event2")
      event("event3")
    }
  }

  test("parse an event's description") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    _metadata:
      |      description: Some description
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event shouldBe buildEvent("event") {
      description = "Some description"
    }
  }

  test("parse an event's excluded platforms") {
    val text = """
      |schemaVersion: 1
      |platforms:
      |  - android
      |  - ios
      |  - web
      |
      |events:
      |  event:
      |    _metadata:
      |      excludedPlatforms:
      |        - ios
      |        - web
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event.excludedPlatforms shouldContainExactly setOf(Platform("ios"), Platform("web"))
  }

  test("fail to parse metadata as an event") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    _metadata:
      |      type: text
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val exception = result.shouldBeFailure<YamlException>()
    exception shouldHaveMessage "Unknown property 'type'. Known properties are: description, excludedPlatforms"
    exception.location shouldBe Location(line = 6, column = 7)
  }

  test("parse a property's description") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property1:
      |      type: text
      |      description: Some description
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeSuccess().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property1") {
      description = "Some description"
    }
  }
})

@Suppress("NOTHING_TO_INLINE")
private inline fun <T> Collection<T>.shouldHaveSingleElement(): T {
  shouldBeSingleton()
  return first()
}
