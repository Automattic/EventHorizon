package com.automattic.eventhorizon.json

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import com.automattic.eventhorizon.Schema
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class JsonGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = JsonGenerator(prettyPrint = true)

  test("generate everything") {
    val schema = Schema.create(
      schemaVersion = 1u,
      availablePlatforms = setOf(
        Platform("web"),
        Platform("ios"),
        Platform("android"),
      ),
      events = Events(
        Event(
          "event_a",
          Property.test("property_a", type = Type.Enum.test("enum_a", "value"), optionalPlatforms = setOf("web")),
          documentation = "Event documentation",
          availablePlatforms = setOf("web", "ios"),
        ),
        Event(
          "event_b",
          Property.test(
            "property_a",
            optionalPlatforms = setOf("android", "ios"),
            type = Type.Enum.test("enum_a", "value"),
          ),
          Property.test(
            "property_b",
            type = Type.Enum.test("enum_b", "value_a", "value_b"),
            documentation = "Property documentation",
          ),
          availablePlatforms = emptySet(),
        ),
      ),
    )

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |[
      |    {
      |        "name": "event_a",
      |        "documentation": "Event documentation",
      |        "availablePlatforms": [
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
      |        "availablePlatforms": [],
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
