package com.automattic.eventhorizon

public fun String.snakeToCamelCase(): String {
  return lowercase()
    .split("_")
    .foldIndexed(StringBuilder()) { index, builder, word ->
      builder.append(if (index == 0) word else word.replaceFirstChar(Char::uppercase))
    }
    .toString()
}
