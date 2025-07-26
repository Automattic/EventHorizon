package com.automattic.eventhorizon.json

import com.automattic.eventhorizon.buildEnumType
import com.automattic.eventhorizon.buildSchema
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class JsonGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = JsonGenerator(prettyPrint = true)

  test("generate everything") {
    val schema = buildSchema {
      platforms("web", "ios", "android")
      events {
        event("event_a") {
          properties {
            enum("property_a", buildEnumType("enuma_a", "value")) {
              optionalPlatforms("web")
            }
          }
          documentation = "Event documentation"
          excludedPlatforms("web", "ios")
        }
        event("event_b") {
          properties {
            enum("property_a", buildEnumType("enum_a", "value")) {
              optionalPlatforms("android", "ios")
            }
            enum("property_b", buildEnumType("enum_b", "value_a", "value_b")) {
              documentation = "Property documentation"
            }
          }
        }
      }
    }

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |[
      |    {
      |        "name": "event_a",
      |        "documentation": "Event documentation",
      |        "excludedPlatforms": [
      |            "web",
      |            "ios"
      |        ],
      |        "properties": [
      |            {
      |                "name": "property_a",
      |                "type": "enum",
      |                "values": [
      |                    "value"
      |                ],
      |                "optionalPlatforms": [
      |                    "web"
      |                ]
      |            }
      |        ]
      |    },
      |    {
      |        "name": "event_b",
      |        "excludedPlatforms": [],
      |        "properties": [
      |            {
      |                "name": "property_a",
      |                "type": "enum",
      |                "values": [
      |                    "value"
      |                ],
      |                "optionalPlatforms": [
      |                    "android",
      |                    "ios"
      |                ]
      |            },
      |            {
      |                "name": "property_b",
      |                "documentation": "Property documentation",
      |                "type": "enum",
      |                "values": [
      |                    "value_a",
      |                    "value_b"
      |                ],
      |                "optionalPlatforms": []
      |            }
      |        ]
      |    }
      |]
    """.trimMargin()
  }
})
