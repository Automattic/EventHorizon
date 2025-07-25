package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Property.Type
import com.automattic.eventhorizon.snakeToCamelCase
import com.automattic.eventhorizon.snakeToPascalCase
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeSpec

internal class EventPropertyEnum(
  private val moduleName: String,
  private val enum: Type.Enum,
) {
  val typeName
    get() = DeclaredTypeName(moduleName, enum.name.snakeToPascalCase())

  private val analyticsValueProperty
    get() = PropertySpec
      .builder("analyticsValue", STRING)
      .getter(FunctionSpec.getterBuilder().addStatement("return rawValue").build())
      .build()

  val typeSpec
    get() = TypeSpec
      .enumBuilder(typeName)
      .addSuperType(STRING)
      .also { builder ->
        enum.values.forEach { value ->
          builder.addEnumCase(value.snakeToCamelCase(), value)
        }
      }
      .addProperty(analyticsValueProperty)
      .build()
}
