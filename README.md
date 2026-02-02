# Event Horizon

A multi-language code generation tool for type-safe event tracking. The system takes YAML schema definitions as input and generates equivalent tracking implementations for Kotlin, Swift, TypeScript, and JSON schema formats.

## Installation

You can install Event Horizon using Homebrew on both macOS and Linux:

```bash
brew tap automattic/build-tools
brew install automattic/build-tools/event-horizon
```

If you prefer, or if Homebrew is not available, you can download prebuilt binaries directly from the [releases page](https://github.com/Automattic/EventHorizon/releases) for both Linux `amd64` and Mac `arm64`.

## Input Schema

```yaml
# Required key defining schema's format version.
schemaVersion: 1

# List of platforms that are available for code generation.
platforms:
  - android
  - ios
  - web
  - desktop

# List of groups to categorize events.
groups:
  # 'ungrouped' key is a reserved keyword.
  group_a:
    # Optional key.
    # If key is not present name is derived from the group key.
    name: Some name
    # Optional key.
    description: Some text

# List of events
events:
  user_signup:
    # Optional key.
    # '_metadata' is a reserved keyword and cannot be used as a property
    _metadata:
      # Optional key.
      description: Some description
      # Optional key.
      # Reference to a group defined in groups list.
      # If key is not present the event will be categorized as 'ungrouped'.
      group: group_a
      # Optional key.
      # List of platforms for which event should not be generated. Must use predeclared platforms.
      # If key is not present event will be generated for all platforms.
      excludedPlatforms:
        - android
        - web
    # Optional properties used with an event.
    user_id:
      # Required. Type of the property for the generated code.
      # Must be one of [text, boolean, int, float, <predeclared enum reference>].
      type: text
      # Optional key.
      description: Some description
      # Optional key.
      # Defines if a property can be null. Must be either a boolean or a list of predeclared platforms.
      # If key is not present property is assumed to be not null.
      optional: true
    signup_provider:
      type: signup_type
      description: Some description
      optional:
        - android
        - ios

# List of enums used for property types.
enums:
  signup_type:
    - google
    - facebook
    - apple

# List of property names that are disallowed.
# It can be prefixed with the 'predefined:' keyword to use one of predefined rule sets. For example 'predefined:tracks`.
# Currently supported predefined rule sets are:
#   - tracks
reservedProperties:
  - property_name_1
  - property_name_2
  - predefined:<value>
```

## CLI

The CLI supports two primary operation modes:
- Verification Mode: Validates input YAML schema without generating code.
- Generation Mode: Parses input and generates code using the specified format and platform.

| Option              | Short | Description                             | Required             |
|---------------------|-------|-----------------------------------------|----------------------|
| `--input-file`      | `-i`  | Input schema file                       | Yes                  |
| `--output-path`     | `-o`  | Output path used for generated files    | Yes (for generation) |
| `--output-platform` | `-p`  | Output platform for code generation     | Conditional*         |
| `--output-format`   | `-f`  | Format: `kotlin`, `swift`, `ts`, `json` | Yes (for generation) |
| `--namespace`       | `-n`  | Namespace used for generated code       | No                   |
| `--verify`          | `-v`  | Only run input file verification        | No                   |
| `--help`            | `-h`  | Show help message and exit              | No                   |

*Required for generation when schema declares `availablePlatforms` and format is not `json`.

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

interface Trackable {
  val name: String
  val properties: Map<String, Any>
}

data class UpNextQueueReorderedEvent(
  companion object {
    const val EventName: String = "up_next_queue_reordered"
  }

  val direction: QueueDirection,
  /**
   * The number of slots the episode was moved
   */
  val slots: Long?,
  /**
   * Whether the episode was moved to the next item that will play
   */
  val isNext: Boolean,
  val source: String,
) : Trackable {
  override val name: String
    get() = EventName

  override val properties: Map<String, Any> = buildMap<String, Any> {
    put("direction", direction)
    if (slots != null) {
      put("slots", slots)
    }
    put("is_next", isNext)
    put("source", source)
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

Integration and usage:

```kotlin
val tracker: AnalyticsTracker = TODO()
val eventHorizon = EventHorizon { event ->
  // delegation to analytics tracker
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
  private let eventSink: (Trackable) -> Void

  init(eventSink: @escaping (Trackable) -> Void) {
    self.eventSink = eventSink
  }

  func track(_ event: Trackable) {
    eventSink(event)
  }
}

protocol Trackable {
  var name: String { get }
  var properties: [AnyHashable : Any] { get }
}

/**
 * When the user moves (up or down) one of the episodes
 */
struct UpNextQueueReorderedEvent: Trackable {
  static let eventName: String = "up_next_queue_reordered"

  let direction: QueueDirection
  /**
   * The number of slots the episode was moved
   */
  let slots: Int?
  /**
   * Whether the episode was moved to the next item that will play
   */
  let isNext: Bool
  let episodeUuid: String

  var name: String {
    return UpNextQueueReorderedEvent.eventName
  }

  let properties: [AnyHashable : Any]

  init(
    direction: QueueDirection,
    slots: Int?,
    isNext: Bool,
    episodeUuid: String
  ) {
    self.direction = direction
    self.slots = slots
    self.isNext = isNext
    self.episodeUuid = episodeUuid

    var props: [AnyHashable : Any] = [:]
    props["direction"] = direction.analyticsValue
    if let slots = slots {
      props["slots"] = slots
    }
    props["is_next"] = isNext
    props["episode_uuid"] = episodeUuid
    self.properties = props
  }
}

enum QueueDirection: String {
  case up = "up"
  case down = "down"

  var analyticsValue: String {
    return rawValue
  }
}
```

Integration and usage:

```swift
let tracker: AnalyticsTracker = TODO()
let eventHorizon = EventHorizon { event ->
   // delegation to analytics tracker
}


let event = UpNextQueueReorderedEvent(
  direction: .up,
  slots: 2,
  isNext: false,
  episodeUuid: episode.uuid,
)
eventHorizon.track(event)
```

### TypeScript

Generated code:

```ts
export type Trackable = {
  // When the user moves (up or down) one of the episodes
  "up_next_queue_reordered": {
    direction: QueueDirection;
    // The number of slots the episode was moved
    slots?: number;
    // Whether the episode was moved to the next item that will play
    is_next: boolean;
    episode_uuid: string;
  };
};

export type QueueDirection =
  | "up"
  | "down";
```

Integration and usage:

```ts
const tracker = TODO()

function trackEvent<K extends keyof Trackable>(
  event: K,
  props: Trackable[K] extends undefined ? never : Trackable[K],
): void;

function trackEvent<K extends keyof Trackable>(event: K): void;

function trackEvent<K extends keyof Trackable>(event: K, props?: Trackable[K]): void {
  // delegation to analytics tracker
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
