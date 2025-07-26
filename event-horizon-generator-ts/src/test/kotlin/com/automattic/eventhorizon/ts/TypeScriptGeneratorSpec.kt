package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.PropertyType
import com.automattic.eventhorizon.Schema
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class TypeScriptGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = TypeScriptGenerator(Platform("web"))

  test("generate everything") {
    val schema = Schema.create(
      schemaVersion = 1u,
      availablePlatforms = setOf(Platform("web"), Platform("ios")),
      events = Events(
        Event(
          "event_a",
          Property.test("property_a", type = PropertyType.Enum.test("enum_a", "value")),
          documentation = "Event documentation",
          availablePlatforms = setOf("web"),
        ),
        Event(
          "event_b",
          Property.test(
            "property_a",
            type = PropertyType.Enum.test("enum_a", "value"),
            optionalPlatforms = setOf("web"),
          ),
          Property.test(
            "property_b",
            type = PropertyType.Enum.test("enum_b", "value_a", "value_b"),
            documentation = "Property documentation",
          ),
          availablePlatforms = setOf("web"),
        ),
        Event("event_c", availablePlatforms = setOf("ios")),
      ),
    )

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |export type Trackable = {
      |  // Event documentation
      |  "event_a": {
      |    property_a: EnumA;
      |  };
      |
      |  "event_b": {
      |    property_a?: EnumA;
      |    // Property documentation
      |    property_b: EnumB;
      |  };
      |};
      |
      |export type EnumA =
      |    | "value";
      |
      |export type EnumB =
      |    | "value_a"
      |    | "value_b";
      |
    """.trimMargin()
  }
})
