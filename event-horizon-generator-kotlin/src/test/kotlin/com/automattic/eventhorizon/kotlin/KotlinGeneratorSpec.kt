package com.automattic.eventhorizon.kotlin

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

class KotlinGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = KotlinGenerator("dev.sample", Platform("android"))

  test("generate everything") {
    val schema = EventHorizonSchema.create(
      schemaVersion = 1u,
      availablePlatforms = setOf(Platform("android")),
      events = Events(
        Event(
          "event_a",
          Property.test("property_a", type = Type.Enum.test("enum_a", "value")),
          documentation = "Event documentation",
        ),
        Event(
          "event_b",
          Property.test("property_a", type = Type.Enum.test("enum_a", "value"), optionalPlatforms = setOf("android")),
          Property.test(
            "property_b",
            type = Type.Enum.test("enum_b", "value_a", "value_b"),
            documentation = "Property documentation",
          ),
        ),
      ),
    )

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
      | * Event documentation
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
      |   * Property documentation
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
