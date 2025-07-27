package com.automattic.eventhorizon.utils

import arrow.core.raise.Raise
import arrow.core.raise.ensure

internal fun <ProblemT> Raise<ProblemT>.ensureNotBlank(value: String, raise: () -> ProblemT) {
  ensure(value.isNotBlank(), raise)
}

internal fun <ProblemT, ValueT, KeyT> Raise<ProblemT>.ensureNoDuplicatesBy(
  items: Iterable<ValueT>,
  keySelector: (ValueT) -> KeyT,
  raise: (Map<KeyT, Int>) -> ProblemT,
) {
  val duplicates = items.findDuplicatesBy(keySelector)
  ensure(duplicates.isEmpty()) { raise(duplicates) }
}

private fun <T, K> Iterable<T>.findDuplicatesBy(selector: (T) -> K): Map<K, Int> {
  val uniques = mutableSetOf<K>()
  val duplicates = mutableMapOf<K, Int>()
  for (e in this) {
    val key = selector(e)
    if (!uniques.add(key)) {
      duplicates.put(key, duplicates.getOrDefault(key, 1) + 1)
    }
  }
  return duplicates
}
