package com.pocketcasts.eventhorizon.ts

internal fun StringBuilder.appendIndent(count: Int = 2) = repeat(count) { append(' ') }

internal fun StringBuilder.appendNewLine(count: Int = 1) = repeat(count) { append('\n') }
