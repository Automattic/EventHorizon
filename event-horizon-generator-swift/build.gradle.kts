plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(projects.eventHorizon)
  implementation(libs.swiftpoet)

  testImplementation(projects.eventHorizon.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
