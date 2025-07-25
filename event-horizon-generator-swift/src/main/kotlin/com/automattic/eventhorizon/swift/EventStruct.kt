package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import com.automattic.eventhorizon.snakeToCamelCase
import com.automattic.eventhorizon.snakeToPascalCase
import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
import io.outfoxx.swiftpoet.TypeSpec

internal class EventStruct(
  private val moduleName: String,
  private val event: Event,
  private val trackableProtocol: TrackableProtocol,
) {
  private val type
    get() = DeclaredTypeName(moduleName, "${event.name.snakeToPascalCase()}Event")

  private val structProperties
    get() = event.properties
      .associateBy { property ->
        PropertySpec.builder(property.name.snakeToCamelCase(), property.typeName)
          .also { builder -> property.description?.let(builder::addDoc) }
          .build()
      }

  private val Property.typeName: TypeName
    get() {
      val typeName = when (val type = type) {
        is Type.Boolean -> BOOL
        is Type.Number -> NumericAny
        is Type.Text -> STRING
        is Type.Enum -> EventPropertyEnum(moduleName, type).typeName
      }
      return if (isOptional(Platform.Ios)) {
        // https://github.com/outfoxx/swiftpoet/issues/120
        if (typeName == NumericAny) NumericAnyNullable else typeName.makeOptional()
      } else {
        typeName
      }
    }

  private val eventNameProperty
    get() = PropertySpec
      .builder("eventName", STRING, Modifier.STATIC)
      .initializer("%S", event.name)
      .build()

  private val constructor
    get() = FunctionSpec.constructorBuilder()
      .also { builder ->
        structProperties.forEach { (property, _) ->
          builder
            .addParameter(property.name, property.type)
            .addCode("self.%L = %L\n", property.name, property.name)
        }
      }
      .build()

  private val trackableNameGetter
    get() = CodeBlock
      .builder()
      .addStatement("return %T.%N", type, eventNameProperty)
      .build()

  private val trackablePropertiesGetter
    get() = CodeBlock
      .builder()
      .addStatement("var props: %T = [:]", DictionaryAnyHashableAny)
      .also { builder ->
        structProperties.forEach { (structProperty, codeGenProperty) ->
          if (structProperty.type.optional) {
            builder.beginControlFlow("if", "let %L = %L", structProperty.name, structProperty.name)
          }
          if (codeGenProperty.type is Type.Enum) {
            builder.addStatement("props[%S] = %L.analyticsValue", codeGenProperty.name, structProperty.name)
          } else {
            builder.addStatement("props[%S] = %L", codeGenProperty.name, structProperty.name)
          }
          if (structProperty.type.optional) {
            builder.endControlFlow("if")
          }
        }
      }
      .addStatement("return props")
      .build()

  val typeSpec
    get() = TypeSpec
      .structBuilder(type)
      .addProperty(eventNameProperty)
      .addProperties(structProperties.keys)
      .also { builder -> event.description?.let(builder::addDoc) }
      .also { builder ->
        if (structProperties.isNotEmpty()) {
          builder.addFunction(constructor)
        }
      }
      .build()
      .let { typeSpec -> trackableProtocol.conformType(typeSpec, trackableNameGetter, trackablePropertiesGetter) }
}
