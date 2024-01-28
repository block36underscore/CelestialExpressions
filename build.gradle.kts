import java.net.URI

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin library project to get you started.
 * For more details take a look at the 'Building Java & JVM projects' chapter in the Gradle
 * User Manual available at https://docs.gradle.org/7.6/userguide/building_java_projects.html
 */

val githubUser: String by project
val githubToken: String by project

tasks.jar {
    manifest {
        attributes(mapOf("Implementation-Title" to project.name,
            "Implementation-Version" to project.version))
    }
}



plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.7.10"

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    `maven-publish`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.1")

    // This dependency is exported to consumers, that is to say found on their compile classpath.
    api("org.apache.commons:commons-math3:3.6.1")
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}


publishing {
    repositories {
        maven {
            name = "CelestialExpressions"
            url = URI("https://maven.pkg.github.com/block36underscore/CelestialExpressions")
            credentials {
                username = githubUser
                password = githubToken
            }
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = "celestialexpressions"
                artifactId = "celestialexpressions"
                version = "1.4.3"

                from(components["java"])
            }
        }
    }
}
