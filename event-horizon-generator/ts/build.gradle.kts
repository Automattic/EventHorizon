plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(projects.eventHorizon)

  testImplementation(projects.eventHorizon.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
