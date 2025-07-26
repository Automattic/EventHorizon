plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(projects.eventHorizon)
  api(projects.eventHorizonGenerator)
  implementation(libs.kotlinpoet)

  testImplementation(projects.eventHorizon.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
