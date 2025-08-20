plugins {
  // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
  alias(libs.plugins.kotlin.jvm)

  // Apply the application plugin to add support for building a CLI application in Java.
  application
  jacoco
}

repositories {
  // Use Maven Central for resolving dependencies.
  mavenCentral()
}

dependencies {
  // This dependency is used by the application.
  implementation(libs.guava)
  implementation(libs.javalin)
  implementation(libs.jackson.module.kotlin)
  implementation(libs.okhttp)
  implementation(libs.slf4j.simple)
  implementation(libs.kotlinx.coroutines.core)

  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.mockk)
  testImplementation(kotlin("test"))
}

testing {
  suites {
    // Configure the built-in test suite
    val test by getting(JvmTestSuite::class) {
      // Use JUnit Jupiter test framework
      useJUnitJupiter("5.12.1")
    }
  }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(17)
  }
}

application {
  // Define the main class for the application.
  mainClass = "com.zecobranca.MainKt"
}

tasks.jar {
  manifest {
    attributes(
      "Main-Class" to "com.zecobranca.MainKt",
      "Implementation-Title" to "ZeCobranca",
      "Implementation-Version" to project.version,
    )
  }

  // Include all dependencies in the jar
  from(
    configurations.runtimeClasspath.get().map {
      if (it.isDirectory) it else zipTree(it)
    },
  )

  duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.test {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  reports {
    xml.required.set(true)
    html.required.set(true)
  }
  // Exclude specific files from coverage report
  classDirectories.setFrom(
    files(
      classDirectories.files.map {
        fileTree(it) {
          // Add files to exclude here
          exclude(
            "com/zecobranca/main/config/Env.kt",
            "com/zecobranca/main/Main.kt",
          )
        }
      },
    ),
  )
  dependsOn(tasks.test) // Ensure tests are run before generating the report
}

// Optional: Add coverage verification
tasks.jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        minimum = "0.70".toBigDecimal()
      }
    }
  }
}
