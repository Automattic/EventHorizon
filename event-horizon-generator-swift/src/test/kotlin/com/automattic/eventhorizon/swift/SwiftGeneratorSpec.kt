package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.EventHorizonSchema
import com.automattic.eventhorizon.Events
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import kotlin.io.path.readText

class SwiftGeneratorSpec : FunSpec({
  val tempDir = tempdir().toPath()
  val generator = SwiftGenerator("MyModule", Platform("ios"))

  test("generate everything") {
    val schema = EventHorizonSchema.create(
      schemaVersion = 1u,
      availablePlatforms = setOf(Platform("ios"), Platform("android")),
      events = Events(
        Event(
          "event_a",
          Property.test("property_a", type = Type.Enum.test("enum_a", "value")),
          documentation = "Event documentation",
          availablePlatforms = setOf("ios"),
        ),
        Event(
          "event_b",
          Property.test("property_a", type = Type.Enum.test("enum_a", "value"), optionalPlatforms = setOf("ios")),
          Property.test(
            "property_b",
            type = Type.Enum.test("enum_b", "value_a", "value_b"),
            documentation = "Property documentation",
          ),
          availablePlatforms = setOf("ios"),
        ),
        Event("event_c", availablePlatforms = setOf("android")),
      ),
    )

    val file = generator.generate(schema, tempDir)

    file.readText() shouldBe """
      |class EventHorizon {
      |
      |  private let eventSink: (String, [AnyHashable : Any]) -> Void
      |
      |  init(eventSink: @escaping (String, [AnyHashable : Any]) -> Void) {
      |    self.eventSink = eventSink
      |  }
      |
      |  func track(_ event: Trackable) {
      |    eventSink(event.trackableName, event.trackableProperties)
      |  }
      |
      |}
      |
      |protocol Trackable {
      |
      |  var trackableName: String { get }
      |  var trackableProperties: [AnyHashable : Any] { get }
      |
      |}
      |
      |/**
      | * Event documentation */
      |struct EventAEvent : Trackable {
      |
      |  static let eventName: String = "event_a"
      |  let propertyA: EnumA
      |  var trackableName: String {
      |    return EventAEvent.eventName
      |  }
      |  var trackableProperties: [AnyHashable : Any] {
      |    var props: [AnyHashable : Any] = [:]
      |    props["property_a"] = propertyA.analyticsValue
      |    return props
      |  }
      |
      |  init(propertyA: EnumA) {
      |    self.propertyA = propertyA
      |  }
      |
      |}
      |
      |struct EventBEvent : Trackable {
      |
      |  static let eventName: String = "event_b"
      |  let propertyA: EnumA?
      |  /**
      |   * Property documentation */
      |  let propertyB: EnumB
      |  var trackableName: String {
      |    return EventBEvent.eventName
      |  }
      |  var trackableProperties: [AnyHashable : Any] {
      |    var props: [AnyHashable : Any] = [:]
      |    if let propertyA = propertyA {
      |      props["property_a"] = propertyA.analyticsValue
      |    }
      |    props["property_b"] = propertyB.analyticsValue
      |    return props
      |  }
      |
      |  init(propertyA: EnumA?, propertyB: EnumB) {
      |    self.propertyA = propertyA
      |    self.propertyB = propertyB
      |  }
      |
      |}
      |
      |enum EnumA : String {
      |
      |  case value = "value"
      |
      |  var analyticsValue: String {
      |    return rawValue
      |  }
      |
      |}
      |
      |enum EnumB : String {
      |
      |  case valueA = "value_a"
      |  case valueB = "value_b"
      |
      |  var analyticsValue: String {
      |    return rawValue
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
