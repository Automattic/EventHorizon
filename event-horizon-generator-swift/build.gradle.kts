plugins { alias(libs.plugins.kotlin.jvm) }

dependencies {
  api(projects.eventHorizonGenerator)
  implementation(libs.swiftpoet)

  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)
}
