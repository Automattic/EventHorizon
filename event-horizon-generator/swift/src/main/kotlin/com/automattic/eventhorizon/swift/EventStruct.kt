package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Case
import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.PropertyType
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FLOAT
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.INT
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec

internal class EventStruct(
  private val moduleName: String,
  private val event: Event,
  private val trackableProtocol: TrackableProtocol,
  private val platform: Platform,
) {
  private val type
    get() = DeclaredTypeName(moduleName, "${event.name.toString(Case.Pascal)}Event")

  private val structProperties
    get() = event.properties
      .associateBy { property ->
        PropertySpec.builder(property.name.toString(Case.Camel), property.typeName)
          .addModifiers(Modifier.PUBLIC)
          .also { builder -> property.description?.let(builder::addDoc) }
          .build()
      }

  private val Property.typeName: TypeName
    get() {
      val typeName = when (val type = type) {
        is PropertyType.Boolean -> BOOL
        is PropertyType.NumberInt -> INT
        is PropertyType.NumberFloat -> FLOAT
        is PropertyType.Text -> STRING
        is PropertyType.Enum -> EventPropertyEnum(moduleName, type).typeName
      }
      return if (isOptional(platform)) typeName.makeOptional() else typeName
    }

  private val eventNameProperty
    get() = PropertySpec
      .builder("eventName", STRING, Modifier.STATIC)
      .addModifiers(Modifier.PUBLIC)
      .initializer("%S", event.name.rawValue)
      .build()

  private val constructor
    get() = FunctionSpec.constructorBuilder()
      .addModifiers(Modifier.PUBLIC)
      .also { builder ->
        structProperties.forEach { (property, _) ->
          builder
            .addParameter(property.name, property.type)
            .addStatement("self.%L = %L", property.name, property.name)
        }
        if (structProperties.isEmpty()) {
          builder.addStatement("self.%L = [:]", trackableProtocol.propertiesProperty.name)
        } else {
          builder.addStatement("var props: %T = [:]", DictionaryAnyHashableAny)
          structProperties.forEach { (structProperty, codeGenProperty) ->
            if (structProperty.type.optional) {
              builder.beginControlFlow("if", "let %L = %L", structProperty.name, structProperty.name)
            }
            if (codeGenProperty.type is PropertyType.Enum) {
              builder.addStatement("props[%S] = %L.analyticsValue", codeGenProperty.name.rawValue, structProperty.name)
            } else {
              builder.addStatement("props[%S] = %L", codeGenProperty.name.rawValue, structProperty.name)
            }
            if (structProperty.type.optional) {
              builder.endControlFlow("if")
            }
          }
          builder.addStatement("self.%L = props", trackableProtocol.propertiesProperty.name)
        }
      }
      .build()

  private val trackableNameGetter
    get() = CodeBlock
      .builder()
      .addStatement("return %T.%N", type, eventNameProperty)
      .build()

  val typeSpec
    get() = TypeSpec
      .structBuilder(type)
      .addModifiers(Modifier.PUBLIC)
      .addFunction(constructor)
      .addProperty(eventNameProperty)
      .addProperties(structProperties.keys)
      .also { builder -> event.description?.let(builder::addDoc) }
      .build()
      .let { typeSpec -> trackableProtocol.conformType(typeSpec, trackableNameGetter) }
}
