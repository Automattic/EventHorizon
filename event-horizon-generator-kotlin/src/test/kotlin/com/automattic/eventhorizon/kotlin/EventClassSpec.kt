package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildEnumType
import com.automattic.eventhorizon.buildEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventClassSpec : FunSpec({
  val trackable = TrackableInterface("dev.sample")

  test("event without properties") {
    val event = buildEvent("event_name")

    val typeSpec = EventClass("dev.sample", event, trackable, Platform("android")).typeSpec

    typeSpec.toString() shouldBe """
      |public data object EventNameEvent : dev.sample.Trackable {
      |  public const val EventName: kotlin.String = "event_name"
      |
      |  override val trackableName: kotlin.String
      |    get() = EventName
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.emptyMap<kotlin.String, kotlin.Any>()
      |}
      |
    """.trimMargin()
  }

  test("event with properties") {
    val event = buildEvent("event_name") {
      properties {
        text("property_one")
        number("property_two")
        boolean("property_three")
        enum("property_four", buildEnumType("enum_name", "value"))
      }
    }

    val typeSpec = EventClass("dev.sample", event, trackable, Platform("android")).typeSpec

    typeSpec.toString() shouldBe """
      |public data class EventNameEvent(
      |  public val propertyOne: kotlin.String,
      |  public val propertyTwo: kotlin.Number,
      |  public val propertyThree: kotlin.Boolean,
      |  public val propertyFour: dev.sample.EnumName,
      |) : dev.sample.Trackable {
      |  override val trackableName: kotlin.String
      |    get() = EventName
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.buildMap<kotlin.String, kotlin.Any> {
      |      put("property_one", propertyOne)
      |      put("property_two", propertyTwo)
      |      put("property_three", propertyThree)
      |      put("property_four", propertyFour)
      |    }
      |
      |  public companion object {
      |    public const val EventName: kotlin.String = "event_name"
      |  }
      |}
      |
    """.trimMargin()
  }

  test("event comment") {
    val event = buildEvent("event_name") {
      description = "Some description"
    }

    val typeSpec = EventClass("dev.sample", event, trackable, Platform("android")).typeSpec

    typeSpec.toString() shouldBe """
      |/**
      | * Some description
      | */
      |public data object EventNameEvent : dev.sample.Trackable {
      |  public const val EventName: kotlin.String = "event_name"
      |
      |  override val trackableName: kotlin.String
      |    get() = EventName
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.emptyMap<kotlin.String, kotlin.Any>()
      |}
      |
    """.trimMargin()
  }

  test("property_comment") {
    val event = buildEvent("event_name") {
      properties {
        text("property_one") {
          description = "Description 1"
        }
        text("property_two")
        text("property_three") {
          description = "Description 2"
        }
      }
    }

    val typeSpec = EventClass("dev.sample", event, trackable, Platform("android")).typeSpec

    typeSpec.toString() shouldBe """
      |public data class EventNameEvent(
      |  /**
      |   * Description 1
      |   */
      |  public val propertyOne: kotlin.String,
      |  public val propertyTwo: kotlin.String,
      |  /**
      |   * Description 2
      |   */
      |  public val propertyThree: kotlin.String,
      |) : dev.sample.Trackable {
      |  override val trackableName: kotlin.String
      |    get() = EventName
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.buildMap<kotlin.String, kotlin.Any> {
      |      put("property_one", propertyOne)
      |      put("property_two", propertyTwo)
      |      put("property_three", propertyThree)
      |    }
      |
      |  public companion object {
      |    public const val EventName: kotlin.String = "event_name"
      |  }
      |}
      |
    """.trimMargin()
  }

  test("nullable property") {
    val event = buildEvent("event_name") {
      properties {
        text("property_one") {
          optionalPlatforms("web")
        }
        text("property_two") {
          optionalPlatforms("ios")
        }
        text("property_three") {
          optionalPlatforms("android")
        }
      }
    }

    val typeSpec = EventClass("dev.sample", event, trackable, Platform("android")).typeSpec

    typeSpec.toString() shouldBe """
      |public data class EventNameEvent(
      |  public val propertyOne: kotlin.String,
      |  public val propertyTwo: kotlin.String,
      |  public val propertyThree: kotlin.String?,
      |) : dev.sample.Trackable {
      |  override val trackableName: kotlin.String
      |    get() = EventName
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.buildMap<kotlin.String, kotlin.Any> {
      |      put("property_one", propertyOne)
      |      put("property_two", propertyTwo)
      |      if (propertyThree != null) {
      |        put("property_three", propertyThree)
      |      }
      |    }
      |
      |  public companion object {
      |    public const val EventName: kotlin.String = "event_name"
      |  }
      |}
      |
    """.trimMargin()
  }
})
