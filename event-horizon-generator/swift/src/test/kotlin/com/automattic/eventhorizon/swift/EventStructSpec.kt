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
      |  public var analyticsName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let analyticsProperties: [Swift.String : Swift.CustomStringConvertible]
      |  public var description: Swift.String {
      |    return "EventNameEvent"
      |  }
      |
      |  public init() {
      |    self.analyticsProperties = [:]
      |  }
      |
      |  public static func ==(lhs: MyModule.EventNameEvent, rhs: MyModule.EventNameEvent) -> Swift.Bool {
      |    return true
      |  }
      |
      |  public func hash(into hasher: inout Swift.Hasher) {
      |    // no-op
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
      |  public var analyticsName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let analyticsProperties: [Swift.String : Swift.CustomStringConvertible]
      |  public var description: Swift.String {
      |    var parts: [Swift.String] = []
      |    parts.append("propertyOne: \(propertyOne)")
      |    parts.append("propertyTwo: \(propertyTwo)")
      |    parts.append("propertyThree: \(propertyThree)")
      |    parts.append("propertyFour: \(propertyFour)")
      |    parts.append("propertyFive: \(propertyFive)")
      |    return "EventNameEvent(\(parts.joined(separator: ", ")))"
      |  }
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
      |    var _props: [Swift.String : Swift.CustomStringConvertible] = [:]
      |    _props["property_one"] = propertyOne
      |    _props["property_two"] = propertyTwo
      |    _props["property_three"] = propertyThree
      |    _props["property_four"] = propertyFour.analyticsValue
      |    _props["property_five"] = propertyFive
      |    self.analyticsProperties = _props
      |  }
      |
      |  public static func ==(lhs: MyModule.EventNameEvent, rhs: MyModule.EventNameEvent) -> Swift.Bool {
      |    return
      |      lhs.propertyOne == rhs.propertyOne &&
      |      lhs.propertyTwo == rhs.propertyTwo &&
      |      lhs.propertyThree == rhs.propertyThree &&
      |      lhs.propertyFour == rhs.propertyFour &&
      |      lhs.propertyFive == rhs.propertyFive
      |  }
      |
      |  public func hash(into hasher: inout Swift.Hasher) {
      |    hasher.combine(propertyOne)
      |    hasher.combine(propertyTwo)
      |    hasher.combine(propertyThree)
      |    hasher.combine(propertyFour)
      |    hasher.combine(propertyFive)
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
      |  public var analyticsName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let analyticsProperties: [Swift.String : Swift.CustomStringConvertible]
      |  public var description: Swift.String {
      |    return "EventNameEvent"
      |  }
      |
      |  public init() {
      |    self.analyticsProperties = [:]
      |  }
      |
      |  public static func ==(lhs: MyModule.EventNameEvent, rhs: MyModule.EventNameEvent) -> Swift.Bool {
      |    return true
      |  }
      |
      |  public func hash(into hasher: inout Swift.Hasher) {
      |    // no-op
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
      |  public var analyticsName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let analyticsProperties: [Swift.String : Swift.CustomStringConvertible]
      |  public var description: Swift.String {
      |    var parts: [Swift.String] = []
      |    parts.append("propertyOne: \(propertyOne)")
      |    parts.append("propertyTwo: \(propertyTwo)")
      |    parts.append("propertyThree: \(propertyThree)")
      |    return "EventNameEvent(\(parts.joined(separator: ", ")))"
      |  }
      |
      |  public init(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.String,
      |    propertyThree: Swift.String
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyTwo = propertyTwo
      |    self.propertyThree = propertyThree
      |    var _props: [Swift.String : Swift.CustomStringConvertible] = [:]
      |    _props["property_one"] = propertyOne
      |    _props["property_two"] = propertyTwo
      |    _props["property_three"] = propertyThree
      |    self.analyticsProperties = _props
      |  }
      |
      |  public static func ==(lhs: MyModule.EventNameEvent, rhs: MyModule.EventNameEvent) -> Swift.Bool {
      |    return
      |      lhs.propertyOne == rhs.propertyOne &&
      |      lhs.propertyTwo == rhs.propertyTwo &&
      |      lhs.propertyThree == rhs.propertyThree
      |  }
      |
      |  public func hash(into hasher: inout Swift.Hasher) {
      |    hasher.combine(propertyOne)
      |    hasher.combine(propertyTwo)
      |    hasher.combine(propertyThree)
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
      |  public let propertyThree: Swift.String
      |  public let propertyTwo: Swift.String?
      |  public var analyticsName: Swift.String {
      |    return MyModule.EventNameEvent.eventName
      |  }
      |  public let analyticsProperties: [Swift.String : Swift.CustomStringConvertible]
      |  public var description: Swift.String {
      |    var parts: [Swift.String] = []
      |    parts.append("propertyOne: \(propertyOne)")
      |    parts.append("propertyThree: \(propertyThree)")
      |    parts.append("propertyTwo: \(Swift.String(describing: propertyTwo))")
      |    return "EventNameEvent(\(parts.joined(separator: ", ")))"
      |  }
      |
      |  public init(
      |    propertyOne: Swift.String,
      |    propertyThree: Swift.String,
      |    propertyTwo: Swift.String? = nil
      |  ) {
      |    self.propertyOne = propertyOne
      |    self.propertyThree = propertyThree
      |    self.propertyTwo = propertyTwo
      |    var _props: [Swift.String : Swift.CustomStringConvertible] = [:]
      |    _props["property_one"] = propertyOne
      |    _props["property_three"] = propertyThree
      |    if let propertyTwo = propertyTwo {
      |      _props["property_two"] = propertyTwo
      |    }
      |    self.analyticsProperties = _props
      |  }
      |
      |  public static func ==(lhs: MyModule.EventNameEvent, rhs: MyModule.EventNameEvent) -> Swift.Bool {
      |    return
      |      lhs.propertyOne == rhs.propertyOne &&
      |      lhs.propertyThree == rhs.propertyThree &&
      |      lhs.propertyTwo == rhs.propertyTwo
      |  }
      |
      |  public func hash(into hasher: inout Swift.Hasher) {
      |    hasher.combine(propertyOne)
      |    hasher.combine(propertyThree)
      |    hasher.combine(propertyTwo)
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
