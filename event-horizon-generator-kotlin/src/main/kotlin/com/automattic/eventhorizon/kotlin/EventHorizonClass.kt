package com.automattic.eventhorizon.kotlin

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT

internal class EventHorizonClass(
  private val packageName: String,
  private val trackable: TrackableInterface,
) {
  private val trackFunction
    get() = FunSpec
      .builder("track")
      .addParameter("event", trackable.className)
      .addCode("%N(event.%N, event.%N)", EventSinkProperty, trackable.nameProperty, trackable.propertiesProperty)
      .build()

  val typeSpec
    get() = TypeSpec
      .classBuilder(ClassName(packageName, "EventHorizon"))
      .primaryConstructor(Constructor)
      .addProperty(EventSinkProperty)
      .addFunction(trackFunction)
      .build()
}

private val EventSinkType = LambdaTypeName.get(
  parameters = arrayOf(STRING, MapStringAny),
  returnType = UNIT,
)

private val EventSinkProperty = PropertySpec
  .builder("eventSink", EventSinkType, KModifier.PRIVATE)
  .initializer("eventSink").build()

private val Constructor = FunSpec
  .constructorBuilder()
  .addParameter(EventSinkProperty.name, EventSinkProperty.type)
  .build()
