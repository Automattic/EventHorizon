plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(projects.eventHorizonGenerator)
  implementation(libs.kotlinpoet)

  testImplementation(projects.eventHorizonGenerator.testing)
  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
