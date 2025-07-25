package com.automattic.eventhorizon.swift

import io.outfoxx.swiftpoet.ANY
import io.outfoxx.swiftpoet.DICTIONARY
import io.outfoxx.swiftpoet.DeclaredTypeName
import io.outfoxx.swiftpoet.GenericQualifier
import io.outfoxx.swiftpoet.parameterizedBy

internal val NumericAny = DeclaredTypeName("Swift", "Numeric").qualify(GenericQualifier.ANY)
internal val AnyHashable = DeclaredTypeName("Swift", "AnyHashable")
internal val DictionaryAnyHashableAny = DICTIONARY.parameterizedBy(AnyHashable, ANY)

// https://github.com/outfoxx/swiftpoet/issues/120
internal val NumericAnyNullable = DeclaredTypeName("", "(any Numeric)?")
