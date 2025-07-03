package com.pocketcasts.eventhorizon.kotlin

import com.pocketcasts.eventhorizon.Event
import com.pocketcasts.eventhorizon.Events
import com.pocketcasts.eventhorizon.Property
import com.pocketcasts.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class KotlinGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = KotlinGenerator("dev.sample")

  test("generate everything") {
    val events = Events(
      Event(
        "event_a",
        Property("property_a", Type.Enum("enum_a", "value")),
        description = "Event description",
      ),
      Event(
        "event_b",
        Property("property_a", Type.Enum("enum_a", "value"), optAndroid = true),
        Property("property_b", Type.Enum("enum_b", "value_a", "value_b"), description = "Property description"),
      ),
    )

    val file = generator.generate(events, tempDir)

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
