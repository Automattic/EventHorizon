package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.VOID

internal class EventHorizonClass(
  private val packageName: String,
  private val eventStruct: EventStruct,
) {
  private val eventSinkType = FunctionTypeName.get(
    parameters = arrayOf(eventStruct.typeName),
    returnType = VOID,
  )

  private val eventSinkProperty = PropertySpec
    .builder("eventSink", eventSinkType, Modifier.PRIVATE)
    .build()

  private val eventSinkParameters = ParameterSpec
    .builder(eventSinkProperty.name, eventSinkProperty.type)
    .addAttribute("escaping")
    .build()

  private val constructor = FunctionSpec.constructorBuilder()
    .addModifiers(Modifier.PUBLIC)
    .addParameter(eventSinkParameters)
    .addCode("self.%N = %N\n", eventSinkProperty, eventSinkProperty)
    .build()

  private val typeName
    get() = DeclaredTypeName(packageName, "EventHorizon")

  private val trackFunction
    get() = FunctionSpec.builder("track")
      .addModifiers(Modifier.PUBLIC)
      .addParameter("_", "event", eventStruct.typeName)
      .addCode("%N(event)\n", eventSinkProperty)
      .build()

  val typeSpec
    get() = TypeSpec
      .classBuilder(typeName)
      .addModifiers(Modifier.PUBLIC)
      .addProperty(eventSinkProperty)
      .addFunction(constructor)
      .addFunction(trackFunction)
      .build()
}
