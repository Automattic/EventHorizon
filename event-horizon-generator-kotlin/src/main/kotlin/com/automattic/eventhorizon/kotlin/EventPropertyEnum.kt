package com.automattic.eventhorizon.kotlin

import com.automattic.eventhorizon.PropertyType
import com.automattic.eventhorizon.snakeToPascalCase
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec

internal class EventPropertyEnum(
  private val packageName: String,
  private val enum: PropertyType.Enum,
) {
  val className
    get() = ClassName(packageName, enum.name.snakeToPascalCase())

  val typeSpec
    get() = TypeSpec
      .enumBuilder(className)
      .also { builder ->
        enum.values.forEach { value ->
          builder.addEnumConstant(value.snakeToPascalCase(), createEnumConstantSpec(value))
        }
      }
      .build()

  private fun createEnumConstantSpec(value: String): TypeSpec {
    val toString = FunSpec
      .builder("toString")
      .addModifiers(KModifier.OVERRIDE)
      .addStatement("return %S", value)
      .returns(STRING)
      .build()
    return TypeSpec.anonymousClassBuilder().addFunction(toString).build()
  }
}
