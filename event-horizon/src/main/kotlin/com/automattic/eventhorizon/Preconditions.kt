package com.automattic.eventhorizon

internal fun <T> requireNoDuplicates(collection: Collection<T>, message: (Map<T, Int>) -> String) {
  require(collection.distinct().size == collection.size) {
    val duplicates = collection.groupingBy { it }.eachCount().filter { (_, count) -> count > 1 }
    message(duplicates)
  }
}
