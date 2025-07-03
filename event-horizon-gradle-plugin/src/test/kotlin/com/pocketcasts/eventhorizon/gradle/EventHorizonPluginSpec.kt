package com.pocketcasts.eventhorizon.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import org.gradle.testkit.runner.GradleRunner

class EventHorizonPluginSpec : FunSpec({
  lateinit var gradleRunner: GradleRunner

  cleanBuildDirs()

  beforeTest {
    gradleRunner = GradleRunner.create()
      .withPluginClasspath()
      .withArguments("generateEventHorizon", "--stacktrace")
  }

  test("register code gen task in kotlin project") {
    val fixture = "plugin-kotlin".toFixtureFile()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateEventHorizon").shouldNotBeNull()
  }

  test("fail for non-kotlin project") {
    val fixture = "plugin-no-kotlin".toFixtureFile()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateEventHorizon").shouldBeNull()
  }

  test("register code gen task for kotlin android project") {
    val fixture = "plugin-android-kotlin".toFixtureFile()

    val result = gradleRunner.withProjectDir(fixture).build()

    result.task(":generateEventHorizon").shouldNotBeNull()
  }

  test("fail for android project without kotlin android plugin") {
    val fixture = "plugin-android-no-kotlin".toFixtureFile()

    val result = gradleRunner.withProjectDir(fixture).buildAndFail()

    result.task(":generateEventHorizon").shouldBeNull()
  }
})
