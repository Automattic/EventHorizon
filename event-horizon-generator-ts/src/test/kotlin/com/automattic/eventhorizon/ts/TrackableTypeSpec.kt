package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.PropertyType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TrackableTypeSpec : FunSpec({
  test("event without properties") {
    val events = Events(Event("event_name"))

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_name": undefined;
      |};
    """.trimMargin()
  }

  test("event with properties") {
    val events = Events(
      Event(
        "event_name",
        Property.test("property_one", type = PropertyType.Text),
        Property.test("property_two", type = PropertyType.Number),
        Property.test("property_three", type = PropertyType.Boolean),
        Property.test("property_four", type = PropertyType.Enum.test("enum_name", "value")),
      ),
    )

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_name": {
      |    property_one: string;
      |    property_two: number;
      |    property_three: boolean;
      |    property_four: EnumName;
      |  };
      |};
    """.trimMargin()
  }

  test("event comment") {
    val events = Events(Event("event_name", documentation = "Some documentation"))

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  // Some documentation
      |  "event_name": undefined;
      |};
    """.trimMargin()
  }

  test("property_comment") {
    val events = Events(
      Event(
        "event_name",
        Property.test("property_one", documentation = "Documentation 1"),
        Property.test("property_two"),
        Property.test("property_three", documentation = "Documentation 2"),
      ),
    )

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_name": {
      |    // Documentation 1
      |    property_one: string;
      |    property_two: string;
      |    // Documentation 2
      |    property_three: string;
      |  };
      |};
    """.trimMargin()
  }

  test("nullable property") {
    val events = Events(
      Event(
        "event_name",
        Property.test("property_one", optionalPlatforms = setOf("web")),
        Property.test("property_two", optionalPlatforms = setOf("ios")),
        Property.test("property_three", optionalPlatforms = setOf("android")),
      ),
    )

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_name": {
      |    property_one?: string;
      |    property_two: string;
      |    property_three: string;
      |  };
      |};
    """.trimMargin()
  }

  test("multiple events") {
    val events = Events(
      Event("event_one", Property.test("property")),
      Event("event_two", Property.test("property", type = PropertyType.Boolean)),
      Event("event_three"),
    )

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_one": {
      |    property: string;
      |  };
      |
      |  "event_two": {
      |    property: boolean;
      |  };
      |
      |  "event_three": undefined;
      |};
    """.trimMargin()
  }
})
