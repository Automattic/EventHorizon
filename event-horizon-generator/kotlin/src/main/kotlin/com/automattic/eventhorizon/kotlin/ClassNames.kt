package com.automattic.eventhorizon.kotlin

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STRING

internal val MapStringAny = MAP.parameterizedBy(STRING, ANY)
internal val Parcelable = ClassName("android.os", "Parcelable")
internal val Parcelize = ClassName("kotlinx.parcelize", "Parcelize")
internal val IgnoredOnParcel = ClassName("kotlinx.parcelize", "IgnoredOnParcel")
