package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.ExtensionSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.TypeVariableName
import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint.CONFORMS_TO
import io.outfoxx.swiftpoet.TypeVariableName.Bound.Constraint.SAME_TYPE

internal class AnalyticsValueProtocol(
  val moduleName: String,
) {
  val typeName get() = DeclaredTypeName(moduleName, "AnalyticsValue")

  val typeSpec
    get() = TypeSpec.protocolBuilder(typeName)
      .addModifiers(Modifier.PUBLIC)
      .addProperty(
        PropertySpec.abstractBuilder("analyticsValue", STRING)
          .abstractGetter()
          .build(),
      )
      .build()

  val extensionSpec
    get() = ExtensionSpec.builder(typeName)
      .addConditionalConstraint(
        TypeVariableName("Self", TypeVariableName.bound(CONFORMS_TO, RawRepresentable)),
      )
      .addConditionalConstraint(
        TypeVariableName("Self.RawValue", TypeVariableName.bound(SAME_TYPE, STRING)),
      )
      .addProperty(
        PropertySpec.builder("analyticsValue", STRING, Modifier.PUBLIC)
          .getter(FunctionSpec.getterBuilder().addCode("rawValue\n").build())
          .build(),
      )
      .build()
}
