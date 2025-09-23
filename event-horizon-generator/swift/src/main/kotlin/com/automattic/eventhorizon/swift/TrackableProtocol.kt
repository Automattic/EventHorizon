package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeSpec

internal class TrackableProtocol(
  private val moduleName: String,
) {
  val typeName
    get() = DeclaredTypeName(moduleName, "Trackable")

  val nameProperty
    get() = NameProperty

  val propertiesProperty
    get() = PropertiesProperty

  val typeSpec
    get() = TypeSpec.protocolBuilder(typeName)
      .addModifiers(Modifier.PUBLIC)
      .addProperty(nameProperty.toBuilder().abstractGetter().build())
      .addProperty(propertiesProperty.toBuilder().abstractGetter().build())
      .build()

  fun conformType(type: TypeSpec, nameGetter: CodeBlock, propertiesGetter: CodeBlock): TypeSpec {
    val name = nameProperty
      .toBuilder()
      .getter(FunctionSpec.getterBuilder().addCode(nameGetter).build())
      .build()
    val properties = propertiesProperty
      .toBuilder()
      .getter(FunctionSpec.getterBuilder().addCode(propertiesGetter).build())
      .build()

    return type.toBuilder().addSuperType(typeName).addProperty(name).addProperty(properties).build()
  }
}

private val NameProperty = PropertySpec.builder("trackableName", STRING)
  .addModifiers(Modifier.PUBLIC)
  .build()
private val PropertiesProperty = PropertySpec.builder("trackableProperties", DictionaryAnyHashableAny)
  .addModifiers(Modifier.PUBLIC)
  .build()
