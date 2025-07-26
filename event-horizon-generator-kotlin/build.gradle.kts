plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(projects.eventHorizon)
  implementation(libs.kotlinpoet)

  testImplementation(projects.eventHorizon.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
