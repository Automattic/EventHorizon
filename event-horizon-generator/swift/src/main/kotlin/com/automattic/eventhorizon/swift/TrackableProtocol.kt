package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.BOOL
import io.outfoxx.swiftpoet.CodeBlock
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeName
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
      .addSuperType(Hashable)
      .addSuperType(CustomStringConvertible)
      .addProperty(nameProperty.toBuilder().abstractGetter().build())
      .addProperty(propertiesProperty.toBuilder().abstractGetter().build())
      .build()

  fun conformType(type: TypeSpec, typeName: TypeName, nameGetter: CodeBlock, eventProperties: List<PropertySpec>): TypeSpec {
    val name = nameProperty
      .toBuilder()
      .addModifiers(Modifier.PUBLIC)
      .getter(FunctionSpec.getterBuilder().addCode(nameGetter).build())
      .build()

    val properties = propertiesProperty
      .toBuilder()
      .addModifiers(Modifier.PUBLIC)
      .build()

    val equalityFunction = FunctionSpec.operatorBuilder("==")
      .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
      .addParameter("lhs", typeName)
      .addParameter("rhs", typeName)
      .also { builder ->
        if (eventProperties.isNotEmpty()) {
          builder.addStatement("return")
          eventProperties.forEachIndexed { index, field ->
            builder.addCode("  lhs.%L == rhs.%L", field.name, field.name)
            builder.addStatement(if (index != eventProperties.lastIndex) " &&" else "")
          }
        } else {
          builder.addStatement("return true")
        }
      }
      .returns(BOOL)
      .build()

    val hashingFunction = FunctionSpec.builder("hash")
      .addModifiers(Modifier.PUBLIC)
      .addParameter("into", "hasher", Hasher, Modifier.INOUT)
      .also { builder ->
        if (eventProperties.isNotEmpty()) {
          eventProperties.forEach { field ->
            builder.addStatement("hasher.combine(%L)", field.name)
          }
        } else {
          builder.addStatement("// no-op")
        }
      }
      .build()

    val descriptionProperty = PropertySpec.builder("description", STRING)
      .addModifiers(Modifier.PUBLIC)
      .getter(
        FunctionSpec.getterBuilder()
          .addCode(
            CodeBlock.builder()
              .also { builder ->
                if (eventProperties.isNotEmpty()) {
                  builder.addStatement("var parts: [%T] = []", STRING)
                  eventProperties.forEach { field ->
                    builder.add("parts.append(\"%L: \\(", field.name)
                    if (field.type.optional) {
                      builder.add("%T(describing: %L)", STRING, field.name)
                    } else {
                      builder.add("%L", field.name)
                    }
                    builder.addStatement(")\")")
                  }
                  builder.addStatement("return \"%L(\\(parts.joined(separator: \", \")))\"", type.name)
                } else {
                  builder.addStatement("return \"%L\"", type.name)
                }
              }
              .build(),
          )
          .build(),
      )
      .build()

    return type.toBuilder()
      .addSuperType(this.typeName)
      .addProperty(name)
      .addProperty(properties)
      .addFunction(equalityFunction)
      .addFunction(hashingFunction)
      .addProperty(descriptionProperty)
      .build()
  }
}

private val NameProperty = PropertySpec.builder("name", STRING).build()
private val PropertiesProperty = PropertySpec.builder("properties", DictionaryAnyHashableAny).build()
