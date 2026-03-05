package com.automattic.eventhorizon.kotlin

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class TrackableInterfaceSpec : FunSpec({
  test("trackable interface") {
    val typeSpec = TrackableInterface("dev.sample").typeSpec

    typeSpec.toString() shouldBe """
      |public interface Trackable : android.os.Parcelable {
      |  public val analyticsName: kotlin.String
      |
      |  public val analyticsProperties: kotlin.collections.Map<kotlin.String, kotlin.Any>
      |}
      |
    """.trimMargin()
  }
})
