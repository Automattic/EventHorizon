package com.automattic.eventhorizon.ts

import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.buildEnumType
import com.automattic.eventhorizon.buildEvents
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TrackableTypeSpec : FunSpec({
  test("event without properties") {
    val events = buildEvents {
      event("event_name")
    }

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_name": undefined;
      |};
    """.trimMargin()
  }

  test("event with properties") {
    val events = buildEvents {
      event("event_name") {
        properties {
          text("property_one")
          number("property_two")
          boolean("property_three")
          enum("property_four", buildEnumType("enum_name", "value"))
        }
      }
    }

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

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
    val events = buildEvents {
      event("event_name") {
        documentation = "Some documentation"
      }
    }

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  // Some documentation
      |  "event_name": undefined;
      |};
    """.trimMargin()
  }

  test("property_comment") {
    val events = buildEvents {
      event("event_name") {
        properties {
          text("property_one") {
            documentation = "Documentation 1"
          }
          text("property_two")
          text("property_three") {
            documentation = "Documentation 2"
          }
        }
      }
    }

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

    typeSpec shouldBe """
      |export type Trackable = {
      |  "event_name": {
      |    // Documentation 1
      |    property_one: string;
      |    property_two: string;
      |    // Documentation 2
      |    property_three: string;
      |  };
      |};
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

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

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
    val events = buildEvents {
      event("event_one") {
        properties {
          text("property")
        }
      }
      event("event_two") {
        properties {
          boolean("property")
        }
      }
      event("event_three")
    }

    val typeSpec = TrackableType(events, Platform("web")).typeSpec

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
