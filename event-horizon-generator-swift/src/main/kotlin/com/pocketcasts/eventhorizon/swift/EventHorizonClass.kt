package com.pocketcasts.eventhorizon.swift

import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.FunctionTypeName
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.ParameterSpec
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeSpec
import io.outfoxx.swiftpoet.VOID

internal class EventHorizonClass(
  private val packageName: String,
  private val trackable: TrackableProtocol,
) {
  private val typeName
    get() = DeclaredTypeName(packageName, "EventHorizon")

  private val trackFunction
    get() = FunctionSpec.builder("track")
      .addParameter("_", "event", trackable.typeName)
      .addCode("%N(event.%N, event.%N)\n", EventSinkProperty, trackable.nameProperty, trackable.propertiesProperty)
      .build()

  val typeSpec
    get() = TypeSpec
      .classBuilder(typeName)
      .addProperty(EventSinkProperty)
      .addFunction(Constructor)
      .addFunction(trackFunction)
      .build()
}

private val EventSinkType = FunctionTypeName.get(
  parameters = arrayOf(STRING, DictionaryAnyHashableAny),
  returnType = VOID,
)

private val EventSinkProperty = PropertySpec
  .builder("eventSink", EventSinkType, Modifier.PRIVATE)
  .build()

private val EventSinkParameter = ParameterSpec
  .builder(EventSinkProperty.name, EventSinkProperty.type)
  .addAttribute("escaping")
  .build()

private val Constructor = FunctionSpec.constructorBuilder()
  .addParameter(EventSinkParameter)
  .addCode("self.%N = %N\n", EventSinkProperty, EventSinkProperty)
  .build()
