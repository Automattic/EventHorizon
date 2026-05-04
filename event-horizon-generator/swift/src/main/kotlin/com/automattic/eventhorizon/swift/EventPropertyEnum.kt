package com.automattic.eventhorizon.swift

import com.automattic.eventhorizon.Case
import com.automattic.eventhorizon.PropertyType
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeSpec

internal class EventPropertyEnum(
  private val moduleName: String,
  private val enum: PropertyType.Enum,
) {
  val typeName
    get() = DeclaredTypeName(moduleName, enum.name.toString(Case.Pascal))

  val typeSpec
    get() = TypeSpec
      .enumBuilder(typeName)
      .addModifiers(Modifier.PUBLIC)
      .addSuperType(STRING)
      .addSuperType(AnalyticsValue)
      .also { builder ->
        enum.values.forEach { value ->
          builder.addEnumCase(value.toString(Case.Camel), value.rawValue)
        }
      }
      .build()
}
