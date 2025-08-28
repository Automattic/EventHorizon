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
      groups {
        group("group_a") {
          name = "Custom name"
        }
        group("z_group") {
          description = "Some description"
        }
        group("group_c")
      }
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
          groupKey = "z_group"

          properties {
            enum("property_a", enumType("enum_a", "value")) {
              optionalPlatforms("android", "ios")
            }
            enum("property_b", enumType("enum_b", "value_a", "value_b")) {
              description = "Property description"
            }
          }
        }
        event("event_d") {
          groupKey = "group_a"
        }
        event("event_c") {
          groupKey = "group_a"
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
      |    "groups": [
      |        {
      |            "key": "group_a",
      |            "name": "Custom name"
      |        },
      |        {
      |            "key": "group_c",
      |            "name": "Group c"
      |        },
      |        {
      |            "key": "z_group",
      |            "name": "Z group",
      |            "description": "Some description"
      |        },
      |        {
      |            "key": "ungrouped",
      |            "name": "Ungrouped"
      |        }
      |    ],
      |    "events": {
      |        "group_a": [
      |            {
      |                "key": "event_c",
      |                "name": "Event C",
      |                "excludedPlatforms": [],
      |                "properties": []
      |            },
      |            {
      |                "key": "event_d",
      |                "name": "Event D",
      |                "excludedPlatforms": [],
      |                "properties": []
      |            }
      |        ],
      |        "z_group": [
      |            {
      |                "key": "event_b",
      |                "name": "Event B",
      |                "excludedPlatforms": [],
      |                "properties": [
      |                    {
      |                        "key": "property_b",
      |                        "description": "Property description",
      |                        "type": "enum",
      |                        "values": [
      |                            "value_a",
      |                            "value_b"
      |                        ],
      |                        "optionalPlatforms": []
      |                    },
      |                    {
      |                        "key": "property_a",
      |                        "type": "enum",
      |                        "values": [
      |                            "value"
      |                        ],
      |                        "optionalPlatforms": [
      |                            "android",
      |                            "ios"
      |                        ]
      |                    }
      |                ]
      |            }
      |        ],
      |        "ungrouped": [
      |            {
      |                "key": "event_a",
      |                "name": "Event A",
      |                "description": "Event description",
      |                "excludedPlatforms": [
      |                    "ios",
      |                    "web"
      |                ],
      |                "properties": [
      |                    {
      |                        "key": "property_a",
      |                        "type": "enum",
      |                        "values": [
      |                            "value"
      |                        ],
      |                        "optionalPlatforms": [
      |                            "web"
      |                        ]
      |                    }
      |                ]
      |            }
      |        ]
      |    }
      |}
    """.trimMargin()
  }
})
