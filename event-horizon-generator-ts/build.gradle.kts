plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(projects.eventHorizonGenerator)

  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
