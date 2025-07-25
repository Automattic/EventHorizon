package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class TypeScriptGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = TypeScriptGenerator()

  test("generate everything") {
    val events = Events(
      Event(
        "event_a",
        Property("property_a", Type.Enum("enum_a", "value")),
        description = "Event description",
      ),
      Event(
        "event_b",
        Property("property_a", Type.Enum("enum_a", "value"), optWeb = true),
        Property("property_b", Type.Enum("enum_b", "value_a", "value_b"), description = "Property description"),
      ),
    )

    val file = generator.generate(events, tempDir)

    file.readText() shouldBe """
      |export type Trackable = {
      |  // Event description
      |  "event_a": {
      |    property_a: EnumA;
      |  };
      |
      |  "event_b": {
      |    property_a?: EnumA;
      |    // Property description
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
