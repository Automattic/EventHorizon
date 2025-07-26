plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(projects.eventHorizonGenerator)

  testImplementation(projects.eventHorizonGenerator.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
