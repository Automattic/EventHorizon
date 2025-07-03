package com.pocketcasts.eventhorizon.kotlin

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TrackableInterfaceSpec : FunSpec({
  test("trackable interface") {
    val typeSpec = TrackableInterface("dev.sample").typeSpec

    typeSpec.toString() shouldBe """
      |public interface Trackable {
      |  public val trackableName: kotlin.String
      |
      |  public val trackableProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |}
      |
    """.trimMargin()
  }
})
