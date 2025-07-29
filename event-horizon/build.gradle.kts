plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(libs.arrow.core)

  testImplementation(projects.eventHorizon.testing)
  testImplementation(libs.kotest.arrow)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
