package com.pocketcasts.eventhorizon.json

import com.pocketcasts.eventhorizon.Event
import com.pocketcasts.eventhorizon.Events
import com.pocketcasts.eventhorizon.Property
import com.pocketcasts.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class JsonGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = JsonGenerator(prettyPrint = true)

  test("generate everything") {
    val events = Events(
      Event(
        "event_a",
        Property("property_a", Type.Enum("enum_a", "value"), optWeb = true),
        description = "Event description",
      ),
      Event(
        "event_b",
        Property("property_a", Type.Enum("enum_a", "value"), optIos = true, optAndroid = true),
        Property("property_b", Type.Enum("enum_b", "value_a", "value_b"), description = "Property description"),
      ),
    )

    val file = generator.generate(events, tempDir)

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
