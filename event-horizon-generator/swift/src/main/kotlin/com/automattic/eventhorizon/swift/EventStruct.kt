package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeSpec

internal class EventStruct(
  private val moduleName: String,
) {
  val typeName
    get() = DeclaredTypeName(moduleName, "Event")

  val nameProperty
    get() = NameProperty

  val propertiesProperty
    get() = PropertiesProperty

  val typeSpec
    get() = TypeSpec.structBuilder(typeName)
      .addModifiers(Modifier.PUBLIC)
      .addProperty(nameProperty)
      .addProperty(propertiesProperty)
      .build()
}

private val NameProperty = PropertySpec.builder("name", STRING).addModifiers(Modifier.PUBLIC).build()
private val PropertiesProperty = PropertySpec.builder("properties", DictStringStringConvertible).addModifiers(Modifier.PUBLIC).build()
