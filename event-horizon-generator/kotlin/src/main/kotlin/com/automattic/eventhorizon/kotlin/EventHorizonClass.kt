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
  private val eventSinkType = LambdaTypeName.get(
    parameters = arrayOf(trackable.className),
    returnType = UNIT,
  )

  private val eventSinkProperty = PropertySpec
    .builder("eventSink", eventSinkType, KModifier.PRIVATE)
    .initializer("eventSink").build()

  private val constructor = FunSpec
    .constructorBuilder()
    .addParameter(eventSinkProperty.name, eventSinkProperty.type)
    .build()

  private val trackFunction
    get() = FunSpec
      .builder("track")
      .addParameter("event", trackable.className)
      .addCode("%N(event)", eventSinkProperty)
      .build()

  val typeSpec
    get() = TypeSpec
      .classBuilder(ClassName(packageName, "EventHorizon"))
      .primaryConstructor(constructor)
      .addProperty(eventSinkProperty)
      .addFunction(trackFunction)
      .build()
}
