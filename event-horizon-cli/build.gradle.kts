plugins {
  application
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.graalvm)
}

application {
  mainClass.set("com.automattic.eventhorizon.cli.MainKt")
}

dependencies {
  implementation(projects.eventHorizonGenerator.kotlin)
  implementation(projects.eventHorizonGenerator.swift)
  implementation(projects.eventHorizonGenerator.ts)
  implementation(projects.eventHorizonGenerator.json)
  implementation(projects.eventHorizonParser)
  implementation(libs.clikit)

  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
