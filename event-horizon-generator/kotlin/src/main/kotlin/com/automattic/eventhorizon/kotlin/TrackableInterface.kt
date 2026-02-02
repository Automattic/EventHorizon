package com.automattic.eventhorizon.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal class TrackableInterface(
  private val packageName: String,
) {
  val className
    get() = ClassName(packageName, "Trackable")

  val nameProperty
    get() = NameProperty

  val propertiesProperty
    get() = PropertiesProperty

  val typeSpec
    get() = TypeSpec
      .interfaceBuilder(className)
      .addProperty(nameProperty)
      .addProperty(propertiesProperty)
      .build()

  fun conformType(typeSpec: TypeSpec, nameGetter: CodeBlock, propertiesGetter: CodeBlock): TypeSpec {
    val name = nameProperty
      .toBuilder()
      .addModifiers(KModifier.OVERRIDE)
      .getter(FunSpec.getterBuilder().addCode(nameGetter).build())
      .build()
    val properties = propertiesProperty
      .toBuilder()
      .addModifiers(KModifier.OVERRIDE)
      .getter(FunSpec.getterBuilder().addCode(propertiesGetter).build())
      .build()

    return typeSpec
      .toBuilder()
      .addSuperinterface(className)
      .addProperty(name)
      .addProperty(properties)
      .build()
  }
}

private val NameProperty = PropertySpec.builder("name", STRING).build()
private val PropertiesProperty = PropertySpec.builder("properties", MapStringAny).build()
