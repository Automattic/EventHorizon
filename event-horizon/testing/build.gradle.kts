plugins {
  alias(libs.plugins.kotlin.jvm)
}

dependencies {
  api(projects.eventHorizon)
  api(libs.kotest.assertions)
}
