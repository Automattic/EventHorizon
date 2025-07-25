package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.Property.Type
import com.automattic.eventhorizon.snakeToCamelCase
import com.automattic.eventhorizon.snakeToPascalCase
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.NUMBER
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.buildCodeBlock

internal class EventClass(
  private val packageName: String,
  private val event: Event,
  private val trackableInterface: TrackableInterface,
  private val platform: Platform,
) {
  private val classProperties
    get() = event.properties
      .associateBy { property ->
        PropertySpec
          .builder(property.name.snakeToCamelCase(), property.className)
          .also { builder -> property.description?.let(builder::addKdoc) }
          .build()
      }

  private val Property.className
    get() = when (val type = type) {
      is Type.Boolean -> BOOLEAN
      is Type.Number -> NUMBER
      is Type.Text -> STRING
      is Type.Enum -> EventPropertyEnum(packageName, type).className
    }.copy(nullable = isOptional(platform))

  private val eventNameProperty
    get() = PropertySpec
      .builder("EventName", STRING, KModifier.CONST)
      .initializer("%S", event.name)
      .build()

  private val companionObject
    get() = TypeSpec
      .companionObjectBuilder()
      .addProperty(eventNameProperty)
      .build()

  private val constructor
    get() = FunSpec
      .constructorBuilder()
      .also { builder ->
        classProperties.forEach { (property, _) ->
          builder.addParameter(property.name, property.type)
        }
      }
      .build()

  private val trackableNameGetter
    get() = buildCodeBlock {
      addStatement("return %N", eventNameProperty)
    }

  private val trackablePropertiesGetter
    get() = if (classProperties.isNotEmpty()) {
      propertiesInMap()
    } else {
      emptyPropertyMap()
    }

  private fun propertiesInMap() = buildCodeBlock {
    beginControlFlow("return %M<%T, %T>", BuildMap, STRING, ANY)
    classProperties.forEach { (classProperty, codeGenProperty) ->
      if (classProperty.type.isNullable) {
        beginControlFlow("if (%N != null)", classProperty)
      }
      addStatement("put(%S, %N)", codeGenProperty.name, classProperty)
      if (classProperty.type.isNullable) {
        endControlFlow()
      }
    }
    endControlFlow()
  }

  private fun emptyPropertyMap() = buildCodeBlock {
    addStatement("return %M<%T, %T>()", EmptyMap, STRING, ANY)
  }

  val typeSpec: TypeSpec
    get() {
      val className = ClassName(packageName, "${event.name.snakeToPascalCase()}Event")
      val baseType = if (classProperties.isNotEmpty()) {
        TypeSpec
          .classBuilder(className)
          .primaryConstructor(constructor)
          .addType(companionObject)
      } else {
        TypeSpec
          .objectBuilder(className)
          .addProperty(eventNameProperty)
      }
      val initializerProperties = classProperties.keys.map { property ->
        property.toBuilder().initializer(property.name).build()
      }
      return baseType
        .also { builder -> event.description?.let(builder::addKdoc) }
        .addModifiers(KModifier.DATA)
        .addProperties(initializerProperties)
        .build()
        .let { typeSpec -> trackableInterface.conformType(typeSpec, trackableNameGetter, trackablePropertiesGetter) }
    }
}

private val BuildMap = MemberName("kotlin.collections", "buildMap")
private val EmptyMap = MemberName("kotlin.collections", "emptyMap")
