plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(libs.arrow.core)
  implementation(libs.kaml)

  testImplementation(projects.eventHorizonGenerator.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
