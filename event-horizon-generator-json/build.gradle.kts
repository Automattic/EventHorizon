plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(projects.eventHorizonGenerator)
  implementation(libs.kotlinx.serialization.json)

  testImplementation(projects.eventHorizonGenerator.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
