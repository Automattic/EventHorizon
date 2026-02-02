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
      |public struct EventNameEvent : MyModule.Trackable {
      |
      |  public static let eventName: Swift.String = "event_name"
      |  public var name: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let properties: [Swift.AnyHashable : Swift.Any]
      |
      |  public init() {
      |    self.properties = [:]
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
        int("property_two")
        boolean("property_three")
        enum("property_four", enumType("enum_name", "value"))
        float("property_five")
      }
    }

    val typeSpec = EventStruct("MyModule", event, trackable, Platform("ios")).typeSpec

    typeSpec.toString() shouldBe """
      |public struct EventNameEvent : MyModule.Trackable {
      |
      |  public static let eventName: Swift.String = "event_name"
      |  public let propertyOne: Swift.String
      |  public let propertyTwo: Swift.Int
      |  public let propertyThree: Swift.Bool
      |  public let propertyFour: MyModule.EnumName
      |  public let propertyFive: Swift.Float
      |  public var name: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let properties: [Swift.AnyHashable : Swift.Any]
      |
      |  public init(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.Int,
      |    propertyThree: Swift.Bool,
      |    propertyFour: MyModule.EnumName,
      |    propertyFive: Swift.Float
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyTwo = propertyTwo
      |    self.propertyThree = propertyThree
      |    self.propertyFour = propertyFour
      |    self.propertyFive = propertyFive
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    props["property_one"] = propertyOne
      |    props["property_two"] = propertyTwo
      |    props["property_three"] = propertyThree
      |    props["property_four"] = propertyFour.analyticsValue
      |    props["property_five"] = propertyFive
      |    self.properties = props
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
      |public struct EventNameEvent : MyModule.Trackable {
      |
      |  public static let eventName: Swift.String = "event_name"
      |  public var name: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let properties: [Swift.AnyHashable : Swift.Any]
      |
      |  public init() {
      |    self.properties = [:]
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
      |public struct EventNameEvent : MyModule.Trackable {
      |
      |  public static let eventName: Swift.String = "event_name"
      |  /**
      |   * Description 1 */
      |  public let propertyOne: Swift.String
      |  public let propertyTwo: Swift.String
      |  /**
      |   * Description 2 */
      |  public let propertyThree: Swift.String
      |  public var name: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let properties: [Swift.AnyHashable : Swift.Any]
      |
      |  public init(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.String,
      |    propertyThree: Swift.String
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyTwo = propertyTwo
      |    self.propertyThree = propertyThree
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    props["property_one"] = propertyOne
      |    props["property_two"] = propertyTwo
      |    props["property_three"] = propertyThree
      |    self.properties = props
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
      |public struct EventNameEvent : MyModule.Trackable {
      |
      |  public static let eventName: Swift.String = "event_name"
      |  public let propertyOne: Swift.String
      |  public let propertyTwo: Swift.String?
      |  public let propertyThree: Swift.String
      |  public var name: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let properties: [Swift.AnyHashable : Swift.Any]
      |
      |  public init(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.String?,
      |    propertyThree: Swift.String
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyTwo = propertyTwo
      |    self.propertyThree = propertyThree
      |    var props: [Swift.AnyHashable : Swift.Any] = [:]
      |    props["property_one"] = propertyOne
      |    if let propertyTwo = propertyTwo {
      |      props["property_two"] = propertyTwo
      |    }
      |    props["property_three"] = propertyThree
      |    self.properties = props
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
