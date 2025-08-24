package com.automattic.eventhorizon.json

import com.automattic.eventhorizon.buildSchema
import com.automattic.eventhorizon.enumType
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
          description = "Event description"
          properties {
            enum("property_a", enumType("enuma_a", "value")) {
              optionalPlatforms("web")
            }
          }
          excludedPlatforms("web", "ios")
        }
        event("event_b") {
          properties {
            enum("property_a", enumType("enum_a", "value")) {
              optionalPlatforms("android", "ios")
            }
            enum("property_b", enumType("enum_b", "value_a", "value_b")) {
              description = "Property description"
            }
          }
        }
      }
    }

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |{
      |    "platforms": [
      |        "android",
      |        "ios",
      |        "web"
      |    ],
      |    "events": [
      |        {
      |            "key": "event_a",
      |            "name": "Event A",
      |            "description": "Event description",
      |            "excludedPlatforms": [
      |                "web",
      |                "ios"
      |            ],
      |            "properties": [
      |                {
      |                    "key": "property_a",
      |                    "type": "enum",
      |                    "values": [
      |                        "value"
      |                    ],
      |                    "optionalPlatforms": [
      |                        "web"
      |                    ]
      |                }
      |            ]
      |        },
      |        {
      |            "key": "event_b",
      |            "name": "Event B",
      |            "excludedPlatforms": [],
      |            "properties": [
      |                {
      |                    "key": "property_b",
      |                    "description": "Property description",
      |                    "type": "enum",
      |                    "values": [
      |                        "value_a",
      |                        "value_b"
      |                    ],
      |                    "optionalPlatforms": []
      |                },
      |                {
      |                    "key": "property_a",
      |                    "type": "enum",
      |                    "values": [
      |                        "value"
      |                    ],
      |                    "optionalPlatforms": [
      |                        "android",
      |                        "ios"
      |                    ]
      |                }
      |            ]
      |        }
      |    ]
      |}
    """.trimMargin()
  }
})
