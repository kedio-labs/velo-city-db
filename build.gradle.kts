plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "org.bicycle"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("ch.qos.logback:logback-classic:1.3.11")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("org.yaml:snakeyaml:2.1")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.exposed:exposed-core:0.42.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.42.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.42.1")
    implementation("org.xerial:sqlite-jdbc:3.42.0.1")
    implementation("com.github.ajalt.clikt:clikt:4.2.0")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.mockk:mockk:1.13.7")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.35.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
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
    jvmToolchain(11)
}

application {
    mainClass.set("MainKt")
}