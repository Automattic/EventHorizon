package com.automattic.eventhorizon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.throwable.shouldHaveMessage

class EnumTypeSpec : FunSpec({
  test("throw when enum has no values") {
    val exception = shouldThrow<IllegalArgumentException> {
      PropertyType.Enum("enum_name", emptySet())
    }

    exception shouldHaveMessage "Enum property 'enum_name' has no values"
  }
})
