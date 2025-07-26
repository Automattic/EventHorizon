package com.automattic.eventhorizon

import arrow.core.nonEmptySetOf
import com.automattic.eventhorizon.CaseString.Companion.toCaseString

public fun buildSchema(builderAction: SchemaBuilder.() -> Unit): Schema {
  val builder = SchemaBuilder()
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class SchemaBuilder internal constructor() {
  public var version: ULong = 1u
  private var platforms: Set<String> = emptySet()
  private var events = buildEvents()

  public fun platforms(vararg platforms: String) {
    this.platforms = platforms.toSet()
  }

  public fun events(builderAction: EventsBuilder.() -> Unit) {
    events = buildEvents(builderAction)
  }

  internal fun build() = Schema.create(
    version,
    platforms.mapTo(mutableSetOf(), ::Platform),
    events,
  )
}

public fun buildEvents(builderAction: EventsBuilder.() -> Unit = {}): Events {
  val builder = EventsBuilder()
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class EventsBuilder internal constructor() {
  private val events = mutableListOf<Event>()

  public fun event(name: String, builderAction: EventBuilder.() -> Unit = {}) {
    events += buildEvent(name, builderAction)
  }

  internal fun build(): Events = Events(events)
}

public fun buildEvent(name: String, builderAction: EventBuilder.() -> Unit = {}): Event {
  val builder = EventBuilder(name)
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class EventBuilder internal constructor(
  private val name: String,
) {
  public var description: String? = null
  private var properties = emptyList<Property>()
  private var excludedPlatforms: Set<String> = emptySet()

  public fun excludedPlatforms(vararg platforms: String) {
    excludedPlatforms = platforms.toSet()
  }

  public fun properties(builderAction: PropertyListBuilder.() -> Unit) {
    properties = buildProperties(builderAction)
  }

  internal fun build() = Event(
    name = name.toCaseString(),
    description = description,
    excludedPlatforms = excludedPlatforms.mapTo(mutableSetOf(), ::Platform),
    properties = properties,
  )
}

public fun buildProperties(builderAction: PropertyListBuilder.() -> Unit): List<Property> {
  val builder = PropertyListBuilder()
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class PropertyListBuilder internal constructor() {
  private val properties = mutableListOf<Property>()

  public fun text(name: String, builderAction: BasicPropertyBuilder.() -> Unit = {}) {
    properties += buildProperty(name, PropertyType.Text, builderAction)
  }

  public fun number(name: String, builderAction: BasicPropertyBuilder.() -> Unit = {}) {
    properties += buildProperty(name, PropertyType.Number, builderAction)
  }

  public fun boolean(name: String, builderAction: BasicPropertyBuilder.() -> Unit = {}) {
    properties += buildProperty(name, PropertyType.Boolean, builderAction)
  }

  public fun enum(propertyName: String, enumType: PropertyType.Enum, builderAction: EnumPropertyBuilder.() -> Unit = {}) {
    properties += buildProperty(propertyName, enumType, builderAction)
  }

  internal fun build() = properties.toList()
}

public fun buildProperty(
  name: String,
  type: PropertyType.Basic = PropertyType.Text,
  builderAction: BasicPropertyBuilder.() -> Unit = {},
): Property {
  val builder = BasicPropertyBuilder(name, type)
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class BasicPropertyBuilder internal constructor(
  private val name: String,
  private val type: PropertyType.Basic,
) {
  public var description: String? = null
  private var optionalPlatforms: Set<String> = emptySet()

  public fun optionalPlatforms(platform: String, vararg platforms: String) {
    optionalPlatforms = setOf(platform) + platforms.toSet()
  }

  public fun noOptionalPlatforms() {
    optionalPlatforms = emptySet()
  }

  internal fun build() = Property(
    name,
    type,
    description,
    optionalPlatforms.mapTo(mutableSetOf(), ::Platform),
  )
}

public fun buildProperty(propertyName: String, enumType: PropertyType.Enum, builderAction: EnumPropertyBuilder.() -> Unit = {}): Property {
  val builder = EnumPropertyBuilder(propertyName, enumType)
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class EnumPropertyBuilder internal constructor(
  private val propertyName: String,
  private val enumType: PropertyType.Enum,
) {
  public var description: String? = null
  private var optionalPlatforms: Set<String> = emptySet()

  public fun optionalPlatforms(vararg platforms: String) {
    optionalPlatforms = platforms.toSet()
  }

  internal fun build() = Property(
    propertyName,
    enumType,
    description,
    optionalPlatforms.mapTo(mutableSetOf(), ::Platform),
  )
}

public fun enumType(name: String, value: String, vararg otherValues: String): PropertyType.Enum {
  return PropertyType.Enum(
    name.toCaseString(),
    nonEmptySetOf(value, *otherValues),
  )
}

@DslMarker
internal annotation class SchemaDsl
