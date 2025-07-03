plugins {
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(libs.kaml)

  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
