plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(projects.eventHorizonGenerator)
  implementation(libs.kotlinpoet)

  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
