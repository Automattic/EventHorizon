package com.pocketcasts.eventhorizon.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.gradle.testkit.runner.TaskOutcome.UP_TO_DATE

class CodeGenTaskSpec : FunSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("generateEventHorizon", "--stacktrace")
  }

  test("run codegen task in kotlin project") {
    val fixture = "codegen-kotlin".toFixtureFile()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateEventHorizon")?.outcome shouldBe SUCCESS
    fixture.eventHorizonFile().shouldExist()
  }

  test("run codegen task in android project") {
    val fixture = "codegen-android".toFixtureFile()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateEventHorizon")?.outcome shouldBe SUCCESS
    fixture.eventHorizonFile(isAndroid = true).shouldExist()
  }

  test("fail to run codegen task") {
    val fixture = "codegen-failure".toFixtureFile()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateEventHorizon")?.outcome shouldBe FAILED
  }

  test("task is cacheable") {
    val fixture = "codegen-kotlin".toFixtureFile()

    gradleRunner.withProjectDir(fixture).build()
    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateEventHorizon")?.outcome shouldBe UP_TO_DATE
  }
})
