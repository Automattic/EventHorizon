package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildEvent
import com.automattic.eventhorizon.enumType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventStructSpec : FunSpec({
  val trackable = TrackableProtocol("MyModule")

  test("event without properties") {
    val event = buildEvent("event_name")

    val typeSpec = EventStruct("MyModule", event, trackable, Platform("ios")).typeSpec

    typeSpec.toString() shouldBe """
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  public var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public var trackableProperties: [Swift.AnyHashable : Swift.Any] {
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    return props
      |  }
      |
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
        enum("property_four", enumType("enum_name", "value"))
      }
    }

    val typeSpec = EventStruct("MyModule", event, trackable, Platform("ios")).typeSpec

    typeSpec.toString() shouldBe """
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  let propertyOne: Swift.String
      |  let propertyTwo: any Swift.Numeric
      |  let propertyThree: Swift.Bool
      |  let propertyFour: MyModule.EnumName
      |  public var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public var trackableProperties: [Swift.AnyHashable : Swift.Any] {
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
    val event = buildEvent("event_name") {
      description = "Some description"
    }

    val typeSpec = EventStruct("MyModule", event, trackable, Platform("ios")).typeSpec

    typeSpec.toString() shouldBe """
      |/**
      | * Some description */
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  public var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public var trackableProperties: [Swift.AnyHashable : Swift.Any] {
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    return props
      |  }
      |
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

    val typeSpec = EventStruct("MyModule", event, trackable, Platform("ios")).typeSpec

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
      |  public var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public var trackableProperties: [Swift.AnyHashable : Swift.Any] {
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

    val typeSpec = EventStruct("MyModule", event, trackable, Platform("ios")).typeSpec

    typeSpec.toString() shouldBe """
      |struct EventNameEvent : MyModule.Trackable {
      |
      |  static let eventName: Swift.String = "event_name"
      |  let propertyOne: Swift.String
      |  let propertyTwo: Swift.String?
      |  let propertyThree: Swift.String
      |  public var trackableName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public var trackableProperties: [Swift.AnyHashable : Swift.Any] {
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
