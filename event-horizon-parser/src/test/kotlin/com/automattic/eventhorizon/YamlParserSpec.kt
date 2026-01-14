package com.automattic.eventhorizon

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSingleElement
import io.kotest.matchers.shouldBe
import kotlin.io.path.writeText

class YamlParserSpec : FunSpec({
  val parser = YamlParser()
  val tempFile = tempfile().toPath()

  test("parse an empty schema") {
    tempFile.writeText("")

    val result = parser.parseSchema(tempFile)

    result shouldBeRight Schema.empty
  }

  test("parse a blank schema") {
    tempFile.writeText(" \n ")

    val result = parser.parseSchema(tempFile)

    result shouldBeRight Schema.empty
  }

  test("fail to parse an invalid content") {
    tempFile.writeText("!!!")

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SimpleProblem("Invalid schema content:\n!!!")
  }

  test("parse a schema version") {
    val text = """
      |schemaVersion: 1
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val schema = result.shouldBeRight()
    schema.version shouldBe 1u
  }

  test("fail to parse a negative schema version") {
    val text = """
      |schemaVersion: -1
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SimpleProblem("Invalid value at path '$.schemaVersion'. Expected an unsigned long.")
  }

  test("fail to parse a non-numeric schema version") {
    val text = """
      |schemaVersion: foo
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SimpleProblem("Invalid value at path '$.schemaVersion'. Expected an unsigned long.")
  }

  test("fail to parse unknown root key") {
    val text = """
      |schemaVersion: foo
      |someKey: value
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SimpleProblem("Invalid value at path '$'. Unexpected keys: [someKey].")
  }

  test("fail to parse events without a schema version") {
    val text = """
      |events:
      |  event:
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SimpleProblem("Invalid value at path '$': missing required key 'schemaVersion'.")
  }

  test("parse groups") {
    val text = """
      |schemaVersion: 1
      |
      |groups:
      |  group_a:
      |    name: Custom name A
      |    description: Some description A
      |  group_b:
      |  group_c:
      |    description: Some description C
      |  group_d:
      |    name: Custom name D
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val schema = result.shouldBeRight()
    schema.groups shouldBe buildGroups {
      group("group_a") {
        name = "Custom name A"
        description = "Some description A"
      }
      group("group_b")
      group("group_c") {
        description = "Some description C"
      }
      group("group_d") {
        name = "Custom name D"
      }
    } + Group.empty
  }

  test("fail to parse group with an unknown key") {
    val text = """
      |schemaVersion: 1
      |
      |groups:
      |  group_a:
      |    someKey: foo
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SimpleProblem("Invalid value at path '$.groups.group_a'. Unexpected keys: [someKey].")
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

    val schema = result.shouldBeRight()
    schema.events shouldHaveSingleElement buildEvent("event")
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property", PropertyType.Text)
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property", PropertyType.Number)
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property", PropertyType.Boolean)
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
      |    - value_2
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property", enumType("enum_reference", "value_1", "value_2"))
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

    result shouldBeLeft
      SimpleProblem(
        "Invalid value at path '$.events.event.property.type'. Expected one of 'boolean', 'number', 'text', or a predefined enum, but was 'enum_reference'.",
      )
  }

  test("fail to parse an event with an unkown property that doesn't exist") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    property:
      |      someKey: foo
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SimpleProblem("Invalid value at path '$.events.event.property'. Unexpected keys: [someKey].")
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property") {
      optionalPlatforms("android", "ios")
    }
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property") {
      noOptionalPlatforms()
    }
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property") {
      optionalPlatforms("android")
    }
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property") {
      optionalPlatforms("android", "ios")
    }
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

    result shouldBeLeft
      SimpleProblem("Invalid value at path '$.events.event.property.optional'. Expected a boolean or an array of platforms.")
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

    result shouldBeLeft
      SimpleProblem("Invalid value at path '$.events.event.property.optional'. Expected a boolean or an array of platforms.")
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
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

    val events = result.shouldBeRight().events
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event shouldBe buildEvent("event") {
      description = "Some description"
    }
  }

  test("parse an event's group") {
    val text = """
      |schemaVersion: 1
      |
      |groups:
      |  my_key:
      |
      |events:
      |  event:
      |    _metadata:
      |      group: my_key
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event shouldBe buildEvent("event") {
      groupKey = "my_key"
    }
  }

  test("use 'ungrouped' for an event's as a fallback") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event shouldBe buildEvent("event") {
      groupKey = Group.empty.key.rawValue
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.excludedPlatforms shouldContainExactly platforms("ios", "web")
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

    result shouldBeLeft SimpleProblem("Invalid value at path '$.events.event._metadata'. Unexpected keys: [type].")
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

    val event = result.shouldBeRight().events.shouldHaveSingleElement()
    event.properties shouldHaveSingleElement buildProperty("property1") {
      description = "Some description"
    }
  }

  test("parse empty events") {
    val text = """
      |schemaVersion: 1
      |
      |events:
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result.shouldBeRight().events.shouldBeEmpty()
  }

  test("parse empty enums") {
    val text = """
      |schemaVersion: 1
      |
      |enums:
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result.shouldBeRight().events.shouldBeEmpty()
  }

  test("parse reserved properties") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    prop1:
      |      type: text
      |
      |reservedProperties:
      |  - prop1
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SchemaProblem.ReservedPropertyNames(mapOf("event" to listOf("prop1")))
  }

  test("parse reserved Tracks properties") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    year:
      |      type: text
      |
      |reservedProperties:
      |  - predefined:tracks
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SchemaProblem.ReservedPropertyNames(mapOf("event" to listOf("year")))
  }

  test("parse reserved mixed properties") {
    val text = """
      |schemaVersion: 1
      |
      |events:
      |  event:
      |    prop1:
      |      type: text
      |    year:
      |      type: text
      |
      |reservedProperties:
      |  - predefined:tracks
      |  - prop1
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft SchemaProblem.ReservedPropertyNames(mapOf("event" to listOf("prop1", "year")))
  }

  test("fail to parse reserved predefined unknown properties") {
    val text = """
      |schemaVersion: 1
      |
      |reservedProperties:
      |  - predefined:foo
    """
    tempFile.writeText(text.trimMargin())

    val result = parser.parseSchema(tempFile)

    result shouldBeLeft
      SimpleProblem("Invalid predefined reserved properties 'foo'. Expected one of ${YamlParser.knownReservedProperties}.")
  }
})
