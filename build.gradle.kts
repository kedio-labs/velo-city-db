plugins {
    kotlin("jvm") version "1.9.23"
    application
}

group = "org.bicycle"
version = "0.2.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.5.3")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.3")
    implementation("org.yaml:snakeyaml:2.2")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.exposed:exposed-core:0.48.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.48.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.48.0")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
    implementation("com.github.ajalt.clikt:clikt:4.2.2")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.wiremock:wiremock:3.4.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

tasks {
    val fatJar = register<Jar>("fatJar") {
        dependsOn.addAll(
            listOf(
                "compileJava",
                "compileKotlin",
                "processResources"
            )
        ) // We need this for Gradle optimization to work
        archiveClassifier.set("standalone") // Naming the jar
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        manifest { attributes(mapOf("Main-Class" to application.mainClass)) } // Provided we set it up in the application plugin configuration
        val sourcesMain = sourceSets.main.get()
        val contents = configurations.runtimeClasspath.get()
            .map { if (it.isDirectory) it else zipTree(it) } +
                sourcesMain.output
        from(contents)
    }
    build {
        dependsOn(fatJar) // Trigger fat jar creation during build
    }
}

// see https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html#jvm_test_suite_plugin
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        register<JvmTestSuite>("integrationTest") {
            dependencies {
                implementation(project())
                implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
                implementation("org.xerial:sqlite-jdbc:3.42.0.1")
            }

            sources {
                kotlin {
                    setSrcDirs(listOf("src/it/kotlin"))
                }
                resources {
                    setSrcDirs(listOf("src/it/resources"))
                }
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}



kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}