package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage

class EventHorizonSchemaSpec() : FunSpec({
  test("schema with version 0") {
    val exception = shouldThrow<IllegalArgumentException> {
      EventHorizonSchema.create(
        schemaVersion = 0u,
        events = Events(),
      )
    }
    exception shouldHaveMessage "Schema version must be a positive number. Is: 0"
  }

  test("empty schema has version 0") {
    EventHorizonSchema.Empty.schemaVersion shouldBe 0u
  }
})
