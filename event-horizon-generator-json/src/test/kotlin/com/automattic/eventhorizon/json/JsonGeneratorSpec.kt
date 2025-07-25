package com.automattic.eventhorizon.json

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.EventHorizonSchema
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class JsonGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = JsonGenerator(prettyPrint = true)

  test("generate everything") {
    val schema = EventHorizonSchema.create(
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
          description = "Event description",
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
            description = "Property description",
          ),
        ),
      ),
    )

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |[
      |    {
      |        "name": "event_a",
      |        "description": "Event description",
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
      |                "description": "Property description",
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
