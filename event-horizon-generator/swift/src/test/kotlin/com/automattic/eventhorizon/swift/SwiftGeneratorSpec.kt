package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildSchema
import com.automattic.eventhorizon.enumType
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class SwiftGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = SwiftGenerator("MyModule", Platform("ios"))

  test("generate everything") {
    val schema = buildSchema {
      platforms("ios", "android")
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
              optionalPlatforms("ios")
            }
            enum("property_b", enumType("enum_b", "value_a", "value_b")) {
              description = "Property description"
            }
          }
          excludedPlatforms("android")
        }
        event("event_c") {
          excludedPlatforms("ios")
        }
      }
    }

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |public class EventHorizon {
      |
      |  private let eventSink: (any Trackable) -> Void
      |
      |  public init(eventSink: @escaping (any Trackable) -> Void) {
      |    self.eventSink = eventSink
      |  }
      |
      |  public func track(_ event: any Trackable) {
      |    eventSink(event)
      |  }
      |
      |}
      |
      |public protocol Trackable : Hashable, CustomStringConvertible {
      |
      |  var analyticsName: String { get }
      |  var analyticsProperties: [String : CustomStringConvertible] { get }
      |
      |}
      |
      |/**
      | * Event description */
      |public struct EventAEvent : Trackable {
      |
      |  public static let eventName: String = "event_a"
      |  public let propertyA: EnumA
      |  public var analyticsName: String {
      |    return EventAEvent.eventName
      |  }
      |  public let analyticsProperties: [String : CustomStringConvertible]
      |  public var description: String {
      |    var parts: [String] = []
      |    parts.append("propertyA: \(propertyA)")
      |    return "EventAEvent(\(parts.joined(separator: ", ")))"
      |  }
      |
      |  public init(propertyA: EnumA) {
      |    self.propertyA = propertyA
      |    var _props: [String : CustomStringConvertible] = [:]
      |    _props["property_a"] = propertyA.analyticsValue
      |    self.analyticsProperties = _props
      |  }
      |
      |  public static func ==(lhs: EventAEvent, rhs: EventAEvent) -> Bool {
      |    return
      |      lhs.propertyA == rhs.propertyA
      |  }
      |
      |  public func hash(into hasher: inout Hasher) {
      |    hasher.combine(propertyA)
      |  }
      |
      |}
      |
      |public struct EventBEvent : Trackable {
      |
      |  public static let eventName: String = "event_b"
      |  /**
      |   * Property description */
      |  public let propertyB: EnumB
      |  public let propertyA: EnumA?
      |  public var analyticsName: String {
      |    return EventBEvent.eventName
      |  }
      |  public let analyticsProperties: [String : CustomStringConvertible]
      |  public var description: String {
      |    var parts: [String] = []
      |    parts.append("propertyB: \(propertyB)")
      |    parts.append("propertyA: \(String(describing: propertyA))")
      |    return "EventBEvent(\(parts.joined(separator: ", ")))"
      |  }
      |
      |  public init(propertyB: EnumB, propertyA: EnumA? = nil) {
      |    self.propertyB = propertyB
      |    self.propertyA = propertyA
      |    var _props: [String : CustomStringConvertible] = [:]
      |    _props["property_b"] = propertyB.analyticsValue
      |    if let propertyA = propertyA {
      |      _props["property_a"] = propertyA.analyticsValue
      |    }
      |    self.analyticsProperties = _props
      |  }
      |
      |  public static func ==(lhs: EventBEvent, rhs: EventBEvent) -> Bool {
      |    return
      |      lhs.propertyB == rhs.propertyB &&
      |      lhs.propertyA == rhs.propertyA
      |  }
      |
      |  public func hash(into hasher: inout Hasher) {
      |    hasher.combine(propertyB)
      |    hasher.combine(propertyA)
      |  }
      |
      |}
      |
      |public enum EnumA : String {
      |
      |  case value = "value"
      |
      |  public var analyticsValue: String {
      |    return rawValue
      |  }
      |
      |}
      |
      |public enum EnumB : String {
      |
      |  case valueA = "value_a"
      |  case valueB = "value_b"
      |
      |  public var analyticsValue: String {
      |    return rawValue
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
