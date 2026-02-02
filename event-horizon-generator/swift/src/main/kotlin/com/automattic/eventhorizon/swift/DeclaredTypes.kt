package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.ANY
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.parameterizedBy

internal val AnyHashable = DeclaredTypeName("Swift", "AnyHashable")
internal val Hashable = DeclaredTypeName("Swift", "Hashable")
internal val DictionaryAnyHashableAny = DICTIONARY.parameterizedBy(AnyHashable, ANY)
internal val Hasher = DeclaredTypeName("Swift", "Hasher")
internal val CustomStringConvertible = DeclaredTypeName("Swift", "CustomStringConvertible")
