plugins {
  application
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.graalvm)
}

application {
  mainClass.set("com.pocketcasts.eventhorizon.cli.MainKt")
}

dependencies {
  implementation(projects.eventHorizonGeneratorKotlin)
  implementation(projects.eventHorizonGeneratorSwift)
  implementation(projects.eventHorizonGeneratorTs)
  implementation(projects.eventHorizonGeneratorJson)
  implementation(libs.clikit)

  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
