package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildEnumType
import com.automattic.eventhorizon.buildSchema
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class TypeScriptGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = TypeScriptGenerator(Platform("web"))

  test("generate everything") {
    val schema = buildSchema {
      platforms("web", "ios")
      events {
        event("event_a") {
          properties {
            enum("property_a", buildEnumType("enum_a", "value"))
          }
          description = "Event description"
        }
        event("event_b") {
          properties {
            enum("property_a", buildEnumType("enum_a", "value")) {
              optionalPlatforms("web")
            }
            enum("property_b", buildEnumType("enum_b", "value_a", "value_b")) {
              description = "Property description"
            }
          }
          excludedPlatforms("ios")
        }
        event("event_c") {
          excludedPlatforms("web")
        }
      }
    }

    val file = generator.generate(schema, tempDir)

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
