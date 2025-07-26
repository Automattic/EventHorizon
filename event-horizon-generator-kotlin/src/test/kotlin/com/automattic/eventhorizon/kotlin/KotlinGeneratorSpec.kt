package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildEnumType
import com.automattic.eventhorizon.buildSchema
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class KotlinGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = KotlinGenerator("dev.sample", Platform("android"))

  test("generate everything") {
    val schema = buildSchema {
      platforms("android", "ios")
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
              optionalPlatforms("android")
            }
            enum("property_b", buildEnumType("enum_b", "value_a", "value_b")) {
              description = "Property description"
            }
          }
          excludedPlatforms("ios")
        }
        event("event_c") {
          excludedPlatforms("android")
        }
      }
    }

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |package dev.sample
      |
      |import kotlin.Any
      |import kotlin.String
      |import kotlin.Unit
      |import kotlin.collections.Map
      |import kotlin.collections.buildMap
      |
      |public class EventHorizon(
      |  private val eventSink: (String, Map<String, Any>) -> Unit,
      |) {
      |  public fun track(event: Trackable) {
      |    eventSink(event.trackableName, event.trackableProperties)
      |  }
      |}
      |
      |public interface Trackable {
      |  public val trackableName: String
      |
      |  public val trackableProperties: Map<String, Any>
      |}
      |
      |/**
      | * Event description
      | */
      |public data class EventAEvent(
      |  public val propertyA: EnumA,
      |) : Trackable {
      |  override val trackableName: String
      |    get() = EventName
      |
      |  override val trackableProperties: Map<String, Any>
      |    get() = buildMap<String, Any> {
      |      put("property_a", propertyA)
      |    }
      |
      |  public companion object {
      |    public const val EventName: String = "event_a"
      |  }
      |}
      |
      |public data class EventBEvent(
      |  public val propertyA: EnumA?,
      |  /**
      |   * Property description
      |   */
      |  public val propertyB: EnumB,
      |) : Trackable {
      |  override val trackableName: String
      |    get() = EventName
      |
      |  override val trackableProperties: Map<String, Any>
      |    get() = buildMap<String, Any> {
      |      if (propertyA != null) {
      |        put("property_a", propertyA)
      |      }
      |      put("property_b", propertyB)
      |    }
      |
      |  public companion object {
      |    public const val EventName: String = "event_b"
      |  }
      |}
      |
      |public enum class EnumA {
      |  Value {
      |    override fun toString(): String = "value"
      |  },
      |}
      |
      |public enum class EnumB {
      |  ValueA {
      |    override fun toString(): String = "value_a"
      |  },
      |  ValueB {
      |    override fun toString(): String = "value_b"
      |  },
      |}
      |
    """.trimMargin()
  }
})
