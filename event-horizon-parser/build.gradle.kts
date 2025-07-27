plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  api(projects.eventHorizon)
  implementation(libs.kaml)

  testImplementation(projects.eventHorizon.testing)
  testImplementation(libs.kotest.arrow)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
