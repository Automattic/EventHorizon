plugins {
  `java-gradle-plugin`
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.buildconfig)
}

val pluginName = "eventHorizon"

gradlePlugin {
  plugins {
    create(pluginName) {
      id = "com.pocketcasts.eventhorizon"
      implementationClass = "com.pocketcasts.eventhorizon.gradle.EventHorizonPlugin"
    }
  }
}

buildConfig {
  useKotlinOutput {
    internalVisibility = true
    topLevelConstants = true
  }
  packageName("com.pocketcasts.eventhorizon.gradle")
  buildConfigField("String", "PluginName", "\"$pluginName\"")
}

val fixtureClasspath: Configuration by configurations.creating

tasks.withType<PluginUnderTestMetadata>().configureEach {
  pluginClasspath.from(fixtureClasspath)
}

dependencies {
  compileOnly(libs.android.gradle.api)
  implementation(projects.eventHorizonGenerator)
  implementation(projects.eventHorizonGeneratorKotlin)
  implementation(libs.kotlin.gradle.plugin)

  testImplementation(libs.kotest.assertions)
  testImplementation(libs.kotest.framework)

  fixtureClasspath(libs.android.gradle.plugin)
}
