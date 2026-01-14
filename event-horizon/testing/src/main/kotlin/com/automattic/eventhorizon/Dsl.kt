package com.automattic.eventhorizon

import arrow.core.Either
import arrow.core.getOrElse

public fun buildSchema(builderAction: SchemaBuilder.() -> Unit): Schema {
  val builder = SchemaBuilder()
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class SchemaBuilder internal constructor() {
  public var version: ULong = 1uL
  private var platforms = emptySet<String>()
  private var events = emptyList<Event>()
  private var groups = emptyList<Group>()
  private var reservedProperties = emptySet<String>()

  public fun platforms(vararg platforms: String) {
    this.platforms = platforms.toSet()
  }

  public fun events(builderAction: EventsBuilder.() -> Unit) {
    events = buildEvents(builderAction)
  }

  public fun groups(builderAction: GroupsBuilder.() -> Unit) {
    groups = buildGroups(builderAction)
  }

  public fun reservedProperties(vararg names: String) {
    reservedProperties = names.toSet()
  }

  internal fun build() = Schema(
    version,
    platforms.mapTo(mutableSetOf(), ::Platform),
    groups,
    events,
    reservedProperties.mapTo(mutableSetOf(), ::caseString),
  ).getOrThrow()
}

public fun buildEvents(builderAction: EventsBuilder.() -> Unit = {}): List<Event> {
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

  internal fun build(): List<Event> = events.toList()
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
  public var groupKey: String = Group.empty.key.rawValue
  private var properties = emptyList<Property>()
  private var excludedPlatforms: Set<String> = emptySet()

  public fun excludedPlatforms(vararg platforms: String) {
    excludedPlatforms = platforms.toSet()
  }

  public fun properties(builderAction: PropertyListBuilder.() -> Unit) {
    properties = buildProperties(builderAction)
  }

  internal fun build() = Event(
    name = name,
    groupKey = groupKey,
    description = description,
    excludedPlatforms = excludedPlatforms.mapTo(mutableSetOf(), ::Platform),
    properties = properties,
  ).getOrThrow()
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
  ).getOrThrow()
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
  ).getOrThrow()
}

public fun enumType(name: String, value: String, vararg otherValues: String): PropertyType.Enum {
  return PropertyType.Enum(
    name = name,
    values = setOf(value) + otherValues,
  ).getOrThrow()
}

public fun caseString(value: String): CaseString = CaseString(value).getOrElse {
  throw AssertionError("'$value' cannot be converted to CaseString")
}

public fun platforms(vararg platforms: String): Set<Platform> = platforms.mapTo(mutableSetOf(), ::Platform)

public fun reservedProperties(vararg names: String): Set<CaseString> = names.mapTo(mutableSetOf(), ::caseString)

public fun buildGroups(builderAction: GroupsBuilder.() -> Unit = {}): List<Group> {
  val builder = GroupsBuilder()
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class GroupsBuilder internal constructor() {
  private val groups = mutableListOf<Group>()

  public fun group(key: String, builderAction: GroupBuilder.() -> Unit = {}) {
    groups += buildGroup(key, builderAction)
  }

  internal fun build(): List<Group> = groups.toList()
}

public fun buildGroup(key: String, builderAction: GroupBuilder.() -> Unit = {}): Group {
  val builder = GroupBuilder(key)
  builder.builderAction()
  return builder.build()
}

@SchemaDsl
public class GroupBuilder internal constructor(
  private val key: String,
) {
  public var name: String? = null
  public var description: String? = null

  internal fun build() = Group(
    key = key,
    name = name,
    description = description,
  ).getOrThrow()
}

@DslMarker
internal annotation class SchemaDsl

private fun <L, R> Either<L, R>.getOrThrow(): R = getOrElse { throw AssertionError(it) }
