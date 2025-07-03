package com.pocketcasts.eventhorizon.kotlin

import com.pocketcasts.eventhorizon.Event
import com.pocketcasts.eventhorizon.Property
import com.pocketcasts.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventClassSpec : FunSpec({
  val trackable = TrackableInterface("dev.sample")

  test("event without properties") {
    val event = Event("event_name")

    val typeSpec = EventClass("dev.sample", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |public data object EventNameEvent : dev.sample.Trackable {
      |  override val trackableName: kotlin.String
      |    get() = "event_name"
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.emptyMap<kotlin.String, kotlin.Any>()
      |}
      |
    """.trimMargin()
  }

  test("event with properties") {
    val event = Event(
      "event_name",
      Property("property_one", Type.Text),
      Property("property_two", Type.Number),
      Property("property_three", Type.Boolean),
      Property("property_four", Type.Enum("enum_name", "value")),
    )

    val typeSpec = EventClass("dev.sample", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |public data class EventNameEvent(
      |  public val propertyOne: kotlin.String,
      |  public val propertyTwo: kotlin.Number,
      |  public val propertyThree: kotlin.Boolean,
      |  public val propertyFour: dev.sample.EnumName,
      |) : dev.sample.Trackable {
      |  override val trackableName: kotlin.String
      |    get() = "event_name"
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.buildMap<kotlin.String, kotlin.Any> {
      |      put("property_one", propertyOne)
      |      put("property_two", propertyTwo)
      |      put("property_three", propertyThree)
      |      put("property_four", propertyFour)
      |    }
      |}
      |
    """.trimMargin()
  }

  test("event comment") {
    val event = Event("event_name", description = "Some description")

    val typeSpec = EventClass("dev.sample", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |/**
      | * Some description
      | */
      |public data object EventNameEvent : dev.sample.Trackable {
      |  override val trackableName: kotlin.String
      |    get() = "event_name"
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.emptyMap<kotlin.String, kotlin.Any>()
      |}
      |
    """.trimMargin()
  }

  test("property_comment") {
    val event = Event(
      "event_name",
      Property("property_one", description = "Description 1"),
      Property("property_two"),
      Property("property_three", description = "Description 2"),
    )

    val typeSpec = EventClass("dev.sample", event, trackable).typeSpec

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
      |    get() = "event_name"
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.buildMap<kotlin.String, kotlin.Any> {
      |      put("property_one", propertyOne)
      |      put("property_two", propertyTwo)
      |      put("property_three", propertyThree)
      |    }
      |}
      |
    """.trimMargin()
  }

  test("nullable property") {
    val event = Event(
      "event_name",
      Property("property_one", optWeb = true),
      Property("property_two", optIos = true),
      Property("property_three", optAndroid = true),
    )

    val typeSpec = EventClass("dev.sample", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |public data class EventNameEvent(
      |  public val propertyOne: kotlin.String,
      |  public val propertyTwo: kotlin.String,
      |  public val propertyThree: kotlin.String?,
      |) : dev.sample.Trackable {
      |  override val trackableName: kotlin.String
      |    get() = "event_name"
      |
      |  override val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |    get() = kotlin.collections.buildMap<kotlin.String, kotlin.Any> {
      |      put("property_one", propertyOne)
      |      put("property_two", propertyTwo)
      |      if (propertyThree != null) {
      |        put("property_three", propertyThree)
      |      }
      |    }
      |}
      |
    """.trimMargin()
  }
})
