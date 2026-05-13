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
      |  private let eventSink: (Event) -> Void
      |
      |  public init(eventSink: @escaping (Event) -> Void) {
      |    self.eventSink = eventSink
      |  }
      |
      |  public func track(_ event: Event) {
      |    eventSink(event)
      |  }
      |
      |}
      |
      |public struct Event {
      |
      |  public let name: String
      |  public let properties: [String : CustomStringConvertible]
      |
      |}
      |
      |public extension Event {
      |
      |  /**
      |   * Event description
      |   */
      |  public static func eventA(propertyA: EnumA) -> Event {
      |    var _props: [String : CustomStringConvertible] = [:]
      |    _props["property_a"] = propertyA.analyticsValue
      |    return Event(
      |      name: "event_a",
      |      properties: _props
      |    )
      |  }
      |
      |  /**
      |   * - Parameters:
      |   *   - propertyB: Property description
      |   */
      |  public static func eventB(propertyB: EnumB, propertyA: EnumA? = nil) -> Event {
      |    var _props: [String : CustomStringConvertible] = [:]
      |    _props["property_b"] = propertyB.analyticsValue
      |    if let propertyA {
      |      _props["property_a"] = propertyA.analyticsValue
      |    }
      |    return Event(
      |      name: "event_b",
      |      properties: _props
      |    )
      |  }
      |
      |}
      |
      |public protocol AnalyticsValue {
      |
      |  var analyticsValue: String { get }
      |
      |}
      |
      |extension AnalyticsValue where Self : RawRepresentable, Self.RawValue == String {
      |
      |  public var analyticsValue: String {
      |    rawValue
      |  }
      |}
      |
      |public enum EnumA : String, AnalyticsValue {
      |
      |  case value = "value"
      |
      |}
      |
      |public enum EnumB : String, AnalyticsValue {
      |
      |  case valueA = "value_a"
      |  case valueB = "value_b"
      |
      |}
      |
    """.trimMargin()
  }
})
