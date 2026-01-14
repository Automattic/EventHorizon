plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(projects.eventHorizon)
  implementation(libs.jackson.yaml)

  testImplementation(projects.eventHorizon.testing)
  testImplementation(libs.kotest.arrow)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
