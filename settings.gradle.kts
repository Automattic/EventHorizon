pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
    google()
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "event-horizon-root"

include(
  ":event-horizon",
  ":event-horizon:testing",
  ":event-horizon-cli",
  ":event-horizon-generator",
  ":event-horizon-generator:kotlin",
  ":event-horizon-generator:swift",
  ":event-horizon-generator:ts",
  ":event-horizon-generator:json",
  ":event-horizon-parser",
)
