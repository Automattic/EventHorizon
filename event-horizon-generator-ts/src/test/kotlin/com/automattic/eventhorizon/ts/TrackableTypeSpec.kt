package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TrackableTypeSpec : FunSpec({
  test("event without properties") {
    val events = Events(Event("event_name"))

    val typeSpec = TrackableType(events).typeSpec

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
        Property("property_one", Type.Text),
        Property("property_two", Type.Number),
        Property("property_three", Type.Boolean),
        Property("property_four", Type.Enum("enum_name", "value")),
      ),
    )

    val typeSpec = TrackableType(events).typeSpec

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
    val events = Events(Event("event_name", description = "Some description"))

    val typeSpec = TrackableType(events).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  // Some description
      |  "event_name": undefined;
      |};
    """.trimMargin()
  }

  test("property_comment") {
    val events = Events(
      Event(
        "event_name",
        Property("property_one", description = "Description 1"),
        Property("property_two"),
        Property("property_three", description = "Description 2"),
      ),
    )

    val typeSpec = TrackableType(events).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_name": {
      |    // Description 1
      |    property_one: string;
      |    property_two: string;
      |    // Description 2
      |    property_three: string;
      |  };
      |};
    """.trimMargin()
  }

  test("nullable property") {
    val events = Events(
      Event(
        "event_name",
        Property("property_one", optWeb = true),
        Property("property_two", optIos = true),
        Property("property_three", optAndroid = true),
      ),
    )

    val typeSpec = TrackableType(events).typeSpec

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
      Event("event_one", Property("property")),
      Event("event_two", Property("property", Type.Boolean)),
      Event("event_three"),
    )

    val typeSpec = TrackableType(events).typeSpec

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
