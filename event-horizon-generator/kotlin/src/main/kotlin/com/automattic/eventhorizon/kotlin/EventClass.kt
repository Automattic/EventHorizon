package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.Case
import com.automattic.eventhorizon.Event
import com.automattic.eventhorizon.Platform
import com.automattic.eventhorizon.Property
import com.automattic.eventhorizon.PropertyType
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
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
      .sortedBy { property -> property.isOptional(platform) }
      .associateBy { property ->
        PropertySpec
          .builder(property.name.toString(Case.Camel), property.className)
          .also { builder -> property.description?.let(builder::addKdoc) }
          .build()
      }

  private val Property.className
    get() = when (val type = type) {
      is PropertyType.Boolean -> BOOLEAN
      is PropertyType.NumberInt -> LONG
      is PropertyType.NumberFloat -> DOUBLE
      is PropertyType.Text -> STRING
      is PropertyType.Enum -> EventPropertyEnum(packageName, type).className
    }.copy(nullable = isOptional(platform))

  private val eventNameProperty
    get() = PropertySpec
      .builder("EventName", STRING, KModifier.CONST)
      .initializer("%S", event.name.rawValue)
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
          val parameter = ParameterSpec.builder(property.name, property.type)
            .also { propertyBuilder ->
              if (property.type.isNullable) {
                propertyBuilder.defaultValue("%L", "null")
              }
            }
            .build()
          builder.addParameter(parameter)
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
    beginControlFlow("%M<%T, %T>", BuildMap, STRING, ANY)
    classProperties.forEach { (classProperty, codeGenProperty) ->
      if (classProperty.type.isNullable) {
        beginControlFlow("if (%N != null)", classProperty)
      }
      when (codeGenProperty.type) {
        is PropertyType.Enum -> {
          addStatement("put(%S, %N.toString())", codeGenProperty.name.rawValue, classProperty)
        }

        else -> {
          addStatement("put(%S, %N)", codeGenProperty.name.rawValue, classProperty)
        }
      }
      if (classProperty.type.isNullable) {
        endControlFlow()
      }
    }
    endControlFlow()
  }

  private fun emptyPropertyMap() = buildCodeBlock {
    addStatement("%M<%T, %T>()", EmptyMap, STRING, ANY)
  }

  val typeSpec: TypeSpec
    get() {
      val className = ClassName(packageName, "${event.name.toString(Case.Pascal)}Event")
      val baseType = if (classProperties.isNotEmpty()) {
        TypeSpec
          .classBuilder(className)
          .primaryConstructor(constructor)
          .addType(companionObject)
      } else {
        TypeSpec
          .objectBuilder(className)
          .addProperty(
            eventNameProperty
              .toBuilder()
              .addAnnotation(IgnoredOnParcel)
              .build(),
          )
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
