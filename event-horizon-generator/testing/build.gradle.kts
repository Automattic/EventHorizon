plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(projects.eventHorizonGenerator)
  api(libs.kotest.assertions)
}
