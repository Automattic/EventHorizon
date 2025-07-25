package com.automattic.eventhorizon

public fun String.snakeToPascalCase(): String {
  return lowercase()
    .split("_")
    .fold(StringBuilder()) { builder, word ->
      builder.append(word.replaceFirstChar(Char::uppercase))
    }
    .toString()
}

public fun String.snakeToCamelCase(): String {
  return lowercase()
    .split("_")
    .foldIndexed(StringBuilder()) { index, builder, word ->
      builder.append(if (index == 0) word else word.replaceFirstChar(Char::uppercase))
    }
    .toString()
}
