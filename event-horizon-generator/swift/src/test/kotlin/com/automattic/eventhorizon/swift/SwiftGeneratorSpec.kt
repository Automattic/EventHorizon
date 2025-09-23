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
      |  private let eventSink: (String, [AnyHashable : Any]) -> Void
      |
      |  public init(eventSink: @escaping (String, [AnyHashable : Any]) -> Void) {
      |    self.eventSink = eventSink
      |  }
      |
      |  public func track(_ event: Trackable) {
      |    eventSink(event.trackableName, event.trackableProperties)
      |  }
      |
      |}
      |
      |public protocol Trackable {
      |
      |  var trackableName: String { get }
      |  var trackableProperties: [AnyHashable : Any] { get }
      |
      |}
      |
      |/**
      | * Event description */
      |public struct EventAEvent : Trackable {
      |
      |  public static let eventName: String = "event_a"
      |  public let propertyA: EnumA
      |  public var trackableName: String {
      |    return EventAEvent.eventName
      |  }
      |  public var trackableProperties: [AnyHashable : Any] {
      |    var props: [AnyHashable : Any] = [:]
      |    props["property_a"] = propertyA.analyticsValue
      |    return props
      |  }
      |
      |  public init(propertyA: EnumA) {
      |    self.propertyA = propertyA
      |  }
      |
      |}
      |
      |public struct EventBEvent : Trackable {
      |
      |  public static let eventName: String = "event_b"
      |  public let propertyA: EnumA?
      |  /**
      |   * Property description */
      |  public let propertyB: EnumB
      |  public var trackableName: String {
      |    return EventBEvent.eventName
      |  }
      |  public var trackableProperties: [AnyHashable : Any] {
      |    var props: [AnyHashable : Any] = [:]
      |    if let propertyA = propertyA {
      |      props["property_a"] = propertyA.analyticsValue
      |    }
      |    props["property_b"] = propertyB.analyticsValue
      |    return props
      |  }
      |
      |  public init(propertyA: EnumA?, propertyB: EnumB) {
      |    self.propertyA = propertyA
      |    self.propertyB = propertyB
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
