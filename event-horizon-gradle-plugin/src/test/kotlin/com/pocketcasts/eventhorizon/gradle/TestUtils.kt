package com.pocketcasts.eventhorizon.gradle

import io.kotest.core.TestConfiguration
import java.io.File

internal fun String.toFixtureFile() = File("src/test/fixtures/$this")

internal fun File.eventHorizonFile(fqcn: String = "", isAndroid: Boolean = false) = if (isAndroid) {
  File(this, "build/generated/java/generateEventHorizon/${fqcn.replace(".", "/")}/EventHorizon.kt")
} else {
  File(this, "build/generated/event-horizon/${fqcn.replace(".", "/")}/EventHorizon.kt")
}

internal fun TestConfiguration.cleanBuildDirs() {
  fun cleanUp() {
    File("src/test/fixtures").getBuildDirs().forEach { it.deleteRecursively() }
  }

  beforeSpec { cleanUp() }
  afterTest { cleanUp() }
}

private fun File.getBuildDirs(): List<File> = when (name) {
  "build" -> listOf(this)
  else -> listFiles().orEmpty().flatMap(File::getBuildDirs)
}
