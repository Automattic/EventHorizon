package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.parameterizedBy

internal val CustomStringConvertible = DeclaredTypeName("Swift", "CustomStringConvertible")
internal val DictStringStringConvertible = DICTIONARY.parameterizedBy(STRING, CustomStringConvertible)
internal val AnalyticsValue = DeclaredTypeName("", "AnalyticsValue")
internal val RawRepresentable = DeclaredTypeName("Swift", "RawRepresentable")
