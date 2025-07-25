package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventStructSpec : FunSpec({
  val trackable = TrackableProtocol("MyModule")

  test("event without properties") {
    val event = Event("event_name")

    val typeSpec = EventStruct("MyModule", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  var trackableProperties: [Swift.AnyHashable : Swift.Any] {
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    return props
      |  }
      |
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

    val typeSpec = EventStruct("MyModule", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  let propertyOne: Swift.String
      |  let propertyTwo: any Swift.Numeric
      |  let propertyThree: Swift.Bool
      |  let propertyFour: MyModule.EnumName
      |  var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  var trackableProperties: [Swift.AnyHashable : Swift.Any] {
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    props["property_one"] = propertyOne
      |    props["property_two"] = propertyTwo
      |    props["property_three"] = propertyThree
      |    props["property_four"] = propertyFour.analyticsValue
      |    return props
      |  }
      |
      |  init(
      |    propertyOne: Swift.String,
      |    propertyTwo: any Swift.Numeric,
      |    propertyThree: Swift.Bool,
      |    propertyFour: MyModule.EnumName
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyTwo = propertyTwo
      |    self.propertyThree = propertyThree
      |    self.propertyFour = propertyFour
      |  }
      |
      |}
      |
    """.trimMargin()
  }

  test("event comment") {
    val event = Event("event_name", description = "Some description")

    val typeSpec = EventStruct("MyModule", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |/**
      | * Some description */
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  var trackableProperties: [Swift.AnyHashable : Swift.Any] {
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    return props
      |  }
      |
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

    val typeSpec = EventStruct("MyModule", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  /**
      |   * Description 1 */
      |  let propertyOne: Swift.String
      |  let propertyTwo: Swift.String
      |  /**
      |   * Description 2 */
      |  let propertyThree: Swift.String
      |  var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  var trackableProperties: [Swift.AnyHashable : Swift.Any] {
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    props["property_one"] = propertyOne
      |    props["property_two"] = propertyTwo
      |    props["property_three"] = propertyThree
      |    return props
      |  }
      |
      |  init(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.String,
      |    propertyThree: Swift.String
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyTwo = propertyTwo
      |    self.propertyThree = propertyThree
      |  }
      |
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

    val typeSpec = EventStruct("MyModule", event, trackable).typeSpec

    typeSpec.toString() shouldBe """
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  let propertyOne: Swift.String
      |  let propertyTwo: Swift.String?
      |  let propertyThree: Swift.String
      |  var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  var trackableProperties: [Swift.AnyHashable : Swift.Any] {
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    props["property_one"] = propertyOne
      |    if let propertyTwo = propertyTwo {
      |      props["property_two"] = propertyTwo
      |    }
      |    props["property_three"] = propertyThree
      |    return props
      |  }
      |
      |  init(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.String?,
      |    propertyThree: Swift.String
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyTwo = propertyTwo
      |    self.propertyThree = propertyThree
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
