# Event Horizon

A multi-language code generation tool for type-safe event tracking. It takes YAML schema definitions as input and generates corresponding tracking implementations for Kotlin, Swift, TypeScript, and JSON Schema.

## Installation

You can install Event Horizon with Homebrew on both macOS and Linux:

```bash
brew tap automattic/build-tools
brew install automattic/build-tools/event-horizon
```

If you prefer, or if Homebrew is unavailable, you can download prebuilt binaries directly from the [releases page](https://github.com/Automattic/EventHorizon/releases) for Linux `amd64` and macOS `arm64`.

## Input Schema

```yaml
# Required key that defines the schema format version.
schemaVersion: 1

# List of platforms available for code generation.
platforms:
  - android
  - ios
  - web
  - desktop

# List of groups used to categorize events.
groups:
  # 'ungrouped' is a reserved key.
  group_a:
    # Optional key.
    # If omitted, the name is derived from the group key.
    name: Some name
    # Optional key.
    description: Some description

# List of events.
events:
  user_signup:
    # Optional key.
    # '_metadata' is a reserved keyword and cannot be used as a property.
    _metadata:
      # Optional key.
      description: Some description
      # Optional key.
      # Reference to a group defined in the groups list.
      # If omitted, the event is categorized as 'ungrouped'.
      group: group_a
      # Optional key.
      # List of platforms for which the event should be generated. Must use declared platforms.
      # If omitted, the event is generated for all platforms.
      # If empty, the event is not generated for any platform.
      includedPlatforms:
        - android
        - web
    # Optional properties associated with the event.
    user_id:
      # Required. Property type used in generated code.
      # Must be one of [text, boolean, int, float, <declared enum reference>].
      type: text
      # Optional key.
      description: Some description
      # Optional key.
      # Defines whether a property can be null. Must be either a boolean or a list of declared platforms.
      # If omitted, the property is assumed to be non-null.
      optional: true
    signup_provider:
      type: signup_type
      description: Some description
      optional:
        - android
        - ios

# List of enums used as property types.
enums:
  signup_type:
    - google
    - facebook
    - apple

# List of disallowed property names.
# Prefix an entry with 'predefined:' to use a predefined rule set, for example 'predefined:tracks'.
# Currently supported predefined rule sets:
#   - tracks
reservedProperties:
  - property_name_1
  - property_name_2
  - predefined:<value>
```

## CLI

The CLI supports two primary modes of operation:
- Verification mode: validates the input YAML schema without generating code.
- Generation mode: parses the input and generates code for the specified format and platform.

| Option              | Short | Description                             | Required             |
|---------------------|-------|-----------------------------------------|----------------------|
| `--input-file`      | `-i`  | Input schema file                       | Yes                  |
| `--output-path`     | `-o`  | Output path for generated files         | Yes (for generation) |
| `--output-platform` | `-p`  | Target platform for code generation     | Conditional*         |
| `--output-format`   | `-f`  | Format: `kotlin`, `swift`, `ts`, `json` | Yes (for generation) |
| `--namespace`       | `-n`  | Namespace for generated code            | No                   |
| `--verify`          | `-v`  | Run input file validation only          | No                   |
| `--help`            | `-h`  | Show help and exit                      | No                   |

*Required for generation when the schema declares `platforms` and the format is not `json`.

## Generated code

Event Horizon generates compact code that can be integrated with external analytics tools.

### Kotlin

Generated code:

```kotlin
class EventHorizon(
  private val eventSink: (Trackable) -> Unit,
) {
  fun track(event: Trackable) {
    eventSink(event)
  }
}

interface Trackable : Parcelable {
  val analyticsName: String
  val analyticsProperties: Map<String, Any>
}

/**
 * Emitted when the user moves an episode up or down.
 */
@Parcelize
data class UpNextQueueReorderedEvent(
  companion object {
    const val EventName: String = "up_next_queue_reordered"
  }

  val direction: QueueDirection,
  /**
   * Whether the episode was moved into the next slot to play.
   */
  val isNext: Boolean,
  val episodeUuid: String,
  /**
   * The number of positions the episode was moved.
   */
  val slots: Long? = null,
) : Trackable {
  @IgnoredOnParcel
  override val analyticsName: String
    get() = EventName

  @IgnoredOnParcel
  override val analyticsProperties: Map<String, Any> = buildMap<String, Any> {
    put("direction", direction.toString())
    put("is_next", isNext)
    put("episode_uuid", episodeUuid)
    if (slots != null) {
      put("slots", slots)
    }
  }
}

enum class QueueDirection {
  Up {
    override fun toString(): String = "up"
  },
  Down {
    override fun toString(): String = "down"
  },
}
```

Integration example:

```kotlin
val tracker: AnalyticsTracker = TODO()
val eventHorizon = EventHorizon { event ->
  // Delegate tracking to your analytics system.
}


val event = UpNextQueueReorderedEvent(
  direction = QueueDirection.Up,
  slots = 2,
  isNext = false,
  episodeUuid = episode.uuid,
)
eventHorizon.track(event)
```

### Swift

Generated code:

```swift
class EventHorizon {
  private let eventSink: (Event) -> Void

  init(eventSink: @escaping (Event) -> Void) {
    self.eventSink = eventSink
  }

  func track(_ event: Event) {
    eventSink(event)
  }
}

struct Event {
  let name: String
  let properties: [String : CustomStringConvertible]
}

extension Event {
  /**
   * Emitted when the user moves an episode up or down.
   *
   * - Parameters:
   *   - isNext: Whether the episode was moved into the next slot to play.
   *   - slots: The number of positions the episode was moved.
   */
  static func upNextQueueReordered(
    direction: QueueDirection,
    isNext: Bool,
    episodeUuid: String,
    slots: Int? = nil
  ) -> Event {
    var _props: [String : CustomStringConvertible] = [:]
    _props["direction"] = direction.analyticsValue
    _props["is_next"] = isNext
    _props["episode_uuid"] = episodeUuid
    if let slots {
      _props["slots"] = slots
    }
    return Event(
      name: "up_next_queue_reordered",
      properties: _props
    )
  }
}

protocol AnalyticsValue {
  var analyticsValue: String { get }
}

extension AnalyticsValue where Self : RawRepresentable, Self.RawValue == String {
  var analyticsValue: String {
    rawValue
  }
}

enum QueueDirection : String, AnalyticsValue {
  case up = "up"
  case down = "down"
}
```

Integration example:

```swift
let tracker: AnalyticsTracker = TODO()
let eventHorizon = EventHorizon { event in
  // Delegate tracking to your analytics system.
}


let event = Event.upNextQueueReordered(
  direction: .up,
  isNext: false,
  episodeUuid: episode.uuid,
  slots: 2,
)
eventHorizon.track(event)
```

### TypeScript

Generated code:

```ts
export type Trackable = {
  // Emitted when the user moves an episode up or down.
  "up_next_queue_reordered": {
    direction: QueueDirection;
    // The number of positions the episode was moved.
    slots?: number;
    // Whether the episode was moved into the next slot to play.
    is_next: boolean;
    episode_uuid: string;
  };
};

export type QueueDirection =
  | "up"
  | "down";
```

Integration example:

```ts
const tracker = TODO()

function trackEvent<K extends keyof Trackable>(
  event: K,
  props: Trackable[K] extends undefined ? never : Trackable[K],
): void;

function trackEvent<K extends keyof Trackable>(event: K): void;

function trackEvent<K extends keyof Trackable>(event: K, props?: Trackable[K]): void {
  // Delegate tracking to your analytics system.
}


trackEvent(
  "up_next_queue_reordered",
  {
    direction: "up",
    slots: 2,
    is_next: false,
    episode_uuid: episode.uuid,
  }
)
```
