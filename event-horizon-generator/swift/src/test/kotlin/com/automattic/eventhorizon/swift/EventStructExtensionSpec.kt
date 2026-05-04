package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildEvents
import com.automattic.eventhorizon.enumType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EventStructExtensionSpec : FunSpec({
  val eventStruct = EventStruct("MyModule")
  val analyticsValueProtocol = AnalyticsValueProtocol("MyModule")

  test("event without properties") {
    val events = buildEvents {
      event("event_name")
    }

    val extensionSpec = EventStructExtension(
      "MyModule",
      eventStruct,
      analyticsValueProtocol,
      events,
      Platform("ios"),
    ).extensionSpec

    extensionSpec.toString() shouldBe """
      |public extension MyModule.Event {
      |
      |  public static var eventName: Event {
      |    return Event(
      |      name: "event_name",
      |      properties: [:]
      |    )
      |  }
      |}
      |
    """.trimMargin()
  }

  test("event with properties") {
    val events = buildEvents {
      event("event_name") {
        properties {
          text("property_one")
          int("property_two")
          boolean("property_three")
          enum("property_four", enumType("enum_name", "value"))
          float("property_five")
        }
      }
    }

    val extensionSpec = EventStructExtension(
      "MyModule",
      eventStruct,
      analyticsValueProtocol,
      events,
      Platform("ios"),
    ).extensionSpec

    extensionSpec.toString() shouldBe """
      |public extension MyModule.Event {
      |
      |  public static func eventName(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.Int,
      |    propertyThree: Swift.Bool,
      |    propertyFour: EnumName,
      |    propertyFive: Swift.Float
      |  ) -> Event {
      |    var _props: [Swift.String : Swift.CustomStringConvertible] = [:]
      |    _props["property_one"] = propertyOne
      |    _props["property_two"] = propertyTwo
      |    _props["property_three"] = propertyThree
      |    _props["property_four"] = propertyFour.analyticsValue
      |    _props["property_five"] = propertyFive
      |    return Event(
      |      name: "event_name",
      |      properties: _props
      |    )
      |  }
      |
      |}
      |
    """.trimMargin()
  }

  test("multiple events") {
    val events = buildEvents {
      event("event_a")
      event("event_b") {
        properties {
          text("property_a")
        }
      }
    }

    val extensionSpec = EventStructExtension(
      "MyModule",
      eventStruct,
      analyticsValueProtocol,
      events,
      Platform("ios"),
    ).extensionSpec

    extensionSpec.toString() shouldBe """
      |public extension MyModule.Event {
      |
      |  public static var eventA: Event {
      |    return Event(
      |      name: "event_a",
      |      properties: [:]
      |    )
      |  }
      |  public static func eventB(propertyA: Swift.String) -> Event {
      |    var _props: [Swift.String : Swift.CustomStringConvertible] = [:]
      |    _props["property_a"] = propertyA
      |    return Event(
      |      name: "event_b",
      |      properties: _props
      |    )
      |  }
      |
      |}
      |
    """.trimMargin()
  }

  test("event comment") {
    val events = buildEvents {
      event("event_name") {
        description = "Some description"
      }
    }

    val extensionSpec = EventStructExtension(
      "MyModule",
      eventStruct,
      analyticsValueProtocol,
      events,
      Platform("ios"),
    ).extensionSpec

    extensionSpec.toString() shouldBe """
      |public extension MyModule.Event {
      |
      |  /**
      |   * Some description
      |   */
      |  public static var eventName: Event {
      |    return Event(
      |      name: "event_name",
      |      properties: [:]
      |    )
      |  }
      |}
      |
    """.trimMargin()
  }

  test("property comment") {
    val events = buildEvents {
      event("event_name") {
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
    }

    val extensionSpec = EventStructExtension(
      "MyModule",
      eventStruct,
      analyticsValueProtocol,
      events,
      Platform("ios"),
    ).extensionSpec

    extensionSpec.toString() shouldBe """
      |public extension MyModule.Event {
      |
      |  /**
      |   * - Parameters:
      |   *   - propertyOne: Description 1
      |   *   - propertyThree: Description 2
      |   */
      |  public static func eventName(
      |    propertyOne: Swift.String,
      |    propertyTwo: Swift.String,
      |    propertyThree: Swift.String
      |  ) -> Event {
      |    var _props: [Swift.String : Swift.CustomStringConvertible] = [:]
      |    _props["property_one"] = propertyOne
      |    _props["property_two"] = propertyTwo
      |    _props["property_three"] = propertyThree
      |    return Event(
      |      name: "event_name",
      |      properties: _props
      |    )
      |  }
      |
      |}
      |
    """.trimMargin()
  }

  test("nullable property") {
    val events = buildEvents {
      event("event_name") {
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
    }

    val extensionSpec = EventStructExtension(
      "MyModule",
      eventStruct,
      analyticsValueProtocol,
      events,
      Platform("ios"),
    ).extensionSpec

    extensionSpec.toString() shouldBe """
      |public extension MyModule.Event {
      |
      |  public static func eventName(
      |    propertyOne: Swift.String,
      |    propertyThree: Swift.String,
      |    propertyTwo: Swift.String? = nil
      |  ) -> Event {
      |    var _props: [Swift.String : Swift.CustomStringConvertible] = [:]
      |    _props["property_one"] = propertyOne
      |    _props["property_three"] = propertyThree
      |    if let propertyTwo {
      |      _props["property_two"] = propertyTwo
      |    }
      |    return Event(
      |      name: "event_name",
      |      properties: _props
      |    )
      |  }
      |
      |}
      |
    """.trimMargin()
  }
})
