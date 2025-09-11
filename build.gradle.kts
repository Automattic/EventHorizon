import com.diffplug.gradle.spotless.BaseKotlinExtension
import com.diffplug.gradle.spotless.FormatExtension
import com.diffplug.gradle.spotless.SpotlessExtension
import com.diffplug.gradle.spotless.SpotlessExtensionPredeclare
import com.diffplug.spotless.LineEnding
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
  alias(libs.plugins.buildconfig) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.spotless)
}

val spotlessPluginId = libs.plugins.spotless.get().pluginId
val ktlintVersion = libs.versions.ktlint.get()

allprojects {
  apply(plugin = spotlessPluginId)
  val spotlessFormatters: SpotlessExtension.() -> Unit = {
    lineEndings = LineEnding.UNIX

    val formatExtension: FormatExtension.() -> Unit = {
      trimTrailingWhitespace()
      endWithNewline()
    }

    val baseKotlinExtension: BaseKotlinExtension.() -> Unit = {
      val configOverride = mapOf(
        "ktlint_standard_function-expression-body" to "disabled",
        "ktlint_standard_multiline-expression-wrapping" to "disabled",
        "ktlint_standard_class-signature" to "disabled",
        "ktlint_standard_property-naming" to "disabled",
      )
      ktlint(ktlintVersion).editorConfigOverride(configOverride)
      formatExtension()
    }

    kotlin {
      target("src/**/*.kt")
      baseKotlinExtension()
    }

    kotlinGradle {
      target("*.kts")
      baseKotlinExtension()
    }

    format("misc") {
      target("*.gitignore", "*.toml", "*.md", "*.properties")
      formatExtension()
    }
  }
  configure<SpotlessExtension> {
    spotlessFormatters()
    if (rootProject == project) {
      predeclareDeps()
    }
  }
  if (rootProject == project) {
    configure<SpotlessExtensionPredeclare> { spotlessFormatters() }
  }
}

val jdkVersion = JavaLanguageVersion.of(libs.versions.jdk.get())
val jvmTargetVersion = JvmTarget.fromTarget(libs.versions.jvmTarget.get())
val detektPluginId = libs.plugins.detekt.get().pluginId
val detektVersion = libs.versions.detekt.get()

subprojects {
  group = property("GROUP") as String
  version = property("VERSION_NAME") as String

  plugins.withType<JavaBasePlugin> {
    configure<JavaPluginExtension> {
      toolchain {
        languageVersion.set(jdkVersion)
      }
    }
  }

  tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = jvmTargetVersion.target
    targetCompatibility = jvmTargetVersion.target
  }

  plugins.withType<KotlinBasePlugin>().configureEach {
    configure<KotlinProjectExtension> {
      explicitApi()
    }
  }

  tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
      jvmTarget.set(jvmTargetVersion)
      progressiveMode.set(true)
      allWarningsAsErrors.set(true)
      freeCompilerArgs.addAll("-Xjvm-default=all", "-Xannotation-default-target=param-property")
    }
  }

  apply(plugin = detektPluginId)
  configure<DetektExtension> {
    toolVersion = detektVersion
    allRules = true
    buildUponDefaultConfig = true
  }

  val buildDir = layout.buildDirectory.asFile.get().canonicalPath
  tasks.withType<Detekt>().configureEach {
    jvmTarget = jvmTargetVersion.target
    config.from(rootProject.file("detekt.yml"))
    exclude { it.file.canonicalPath.startsWith(buildDir) }
    reports {
      html.required.set(true)
      xml.required.set(true)
      txt.required.set(true)
    }
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
      events("started", "passed", "failed", "skipped")
      exceptionFormat = TestExceptionFormat.FULL
    }
  }
}
