package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildSchema
import com.automattic.eventhorizon.enumType
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
          description = "Event description"
          properties {
            enum("property_a", enumType("enum_a", "value"))
          }
        }
        event("event_b") {
          properties {
            enum("property_a", enumType("enum_a", "value")) {
              optionalPlatforms("android")
            }
            enum("property_b", enumType("enum_b", "value_a", "value_b")) {
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
      |import android.os.Parcelable
      |import kotlin.Any
      |import kotlin.String
      |import kotlin.Unit
      |import kotlin.collections.Map
      |import kotlin.collections.buildMap
      |import kotlinx.parcelize.IgnoredOnParcel
      |import kotlinx.parcelize.Parcelize
      |
      |public class EventHorizon(
      |  private val eventSink: (Trackable) -> Unit,
      |) {
      |  public fun track(event: Trackable) {
      |    eventSink(event)
      |  }
      |}
      |
      |public interface Trackable : Parcelable {
      |  public val name: String
      |
      |  public val properties: Map<String, Any>
      |}
      |
      |/**
      | * Event description
      | */
      |@Parcelize
      |public data class EventAEvent(
      |  public val propertyA: EnumA,
      |) : Trackable {
      |  @IgnoredOnParcel
      |  override val name: String
      |    get() = EventName
      |
      |  @IgnoredOnParcel
      |  override val properties: Map<String, Any> = buildMap<String, Any> {
      |    put("property_a", propertyA)
      |  }
      |
      |  public companion object {
      |    public const val EventName: String = "event_a"
      |  }
      |}
      |
      |@Parcelize
      |public data class EventBEvent(
      |  /**
      |   * Property description
      |   */
      |  public val propertyB: EnumB,
      |  public val propertyA: EnumA? = null,
      |) : Trackable {
      |  @IgnoredOnParcel
      |  override val name: String
      |    get() = EventName
      |
      |  @IgnoredOnParcel
      |  override val properties: Map<String, Any> = buildMap<String, Any> {
      |    put("property_b", propertyB)
      |    if (propertyA != null) {
      |      put("property_a", propertyA)
      |    }
      |  }
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
