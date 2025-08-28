package com.automattic.eventhorizon

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class GroupSpec : FunSpec({
  test("create a group") {
    val group = Group("group_key", name = "Group name", description = "Description").shouldBeRight()

    group.key shouldBe caseString("group_key")
    group.name shouldBe "Group name"
    group.description shouldBe "Description"
  }

  test("create a group without a description") {
    val group = Group("group_key", name = "Group name", description = null).shouldBeRight()

    group.description.shouldBeNull()
  }

  test("derive a group name from group key when the name is null") {
    val group = Group("group_key", name = null, description = null).shouldBeRight()

    group.name shouldBe "Group key"
  }

  test("empty group exists") {
    val group = Group.empty

    group.key shouldBe caseString("ungrouped")
    group.name shouldBe "Ungrouped"
    group.description.shouldBeNull()
  }

  test("fail to create a group with an empty key") {
    val result = Group("", name = null, description = null)

    result shouldBeLeft GroupProblem.BlankKey
  }

  test("fail to create a group with a blank key") {
    val result = Group(" \t\n ", name = null, description = null)

    result shouldBeLeft GroupProblem.BlankKey
  }
})
