package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Case
import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.PropertyType
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FLOAT
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName

internal class EventStructExtension(
  private val moduleName: String,
  private val eventStruct: EventStruct,
  private val analyticsValueProtocol: AnalyticsValueProtocol,
  private val events: List<Event>,
  private val platform: Platform,
) {
  val extensionSpec
    get() = ExtensionSpec.builder(eventStruct.typeName)
      .addModifiers(Modifier.PUBLIC)
      .also { builder ->
        for (event in events) {
          if (event.properties.isEmpty()) {
            builder.addProperty(event.toExtensionProperty())
          } else {
            builder.addFunction(event.toExtensionFunction())
          }
        }
      }
      .build()

  private fun Event.toExtensionProperty() = PropertySpec
    .builder(swiftName, eventStruct.typeName)
    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    .also { builder -> toDescription()?.let(builder::addDoc) }
    .getter(
      FunctionSpec.getterBuilder()
        .also { builder -> builder.addEventReturn(this, "[:]") }
        .build(),
    )
    .build()

  private fun Event.toExtensionFunction() = FunctionSpec
    .builder(swiftName)
    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
    .also { builder -> toDescription()?.let(builder::addDoc) }
    .also { builder ->
      val properties = extensionProperties

      for (property in properties) {
        builder.addParameter(property.toParameterSpec())
      }
      builder.addStatement("var _props: %T = [:]", DictStringStringConvertible)
      for (property in properties) {
        builder.addPropertyAssignment(property)
      }
      builder.addEventReturn(this, "_props")
    }
    .returns(eventStruct.typeName)
    .build()

  private fun Event.toDescription(): CodeBlock? {
    val propsWithDescriptions = extensionProperties.mapNotNull { property ->
      property.description?.let { propertyDescription -> property to propertyDescription }
    }
    if (description == null && propsWithDescriptions.isEmpty()) {
      return null
    }

    return CodeBlock.builder()
      .also { builder ->
        description?.let(builder::addStatement)
        if (propsWithDescriptions.isNotEmpty()) {
          if (description != null) {
            builder.addStatement("")
          }
          builder.addStatement("- Parameters:")
          for ((prop, desc) in propsWithDescriptions) {
            builder.addStatement("  - ${prop.name.toString(Case.Camel)}: $desc")
          }
        }
      }
      .build()
  }

  private fun Property.toParameterSpec() = ParameterSpec.builder(swiftName, typeName)
    .also { builder ->
      if (isOptionalOnPlatform) {
        builder.defaultValue("%L", "nil")
      }
    }
    .build()

  private fun FunctionSpec.Builder.addEventReturn(event: Event, propertiesExpression: String) {
    addStatement("return %T(", eventStruct.typeName)
      .addStatement("  name: %S,", event.name.rawValue)
      .addStatement("  properties: %L", propertiesExpression)
      .addStatement(")")
  }

  private fun FunctionSpec.Builder.addPropertyAssignment(property: Property) {
    if (property.isOptionalOnPlatform) {
      beginControlFlow("if", "let %L", property.swiftName)
    }
    addStatement("_props[%S] = %L", property.name.rawValue, property.analyticsValueExpression)
    if (property.isOptionalOnPlatform) {
      endControlFlow("if")
    }
  }

  private val Event.swiftName: String
    get() = name.toString(Case.Camel)

  private val Event.extensionProperties: List<Property>
    get() = properties.sortedBy { property -> property.isOptionalOnPlatform }

  private val Property.swiftName: String
    get() = name.toString(Case.Camel)

  private val Property.analyticsValueExpression: String
    get() = when (type) {
      is PropertyType.Enum -> "$swiftName.analyticsValue"
      else -> swiftName
    }

  private val Property.isOptionalOnPlatform: Boolean
    get() = isOptional(platform)

  private val Property.typeName: TypeName
    get() {
      val typeName = when (val type = type) {
        is PropertyType.Boolean -> BOOL
        is PropertyType.NumberInt -> INT
        is PropertyType.NumberFloat -> FLOAT
        is PropertyType.Text -> STRING
        is PropertyType.Enum -> EventPropertyEnum(moduleName, type, analyticsValueProtocol).typeName
      }
      return if (isOptionalOnPlatform) typeName.makeOptional() else typeName
    }
}
