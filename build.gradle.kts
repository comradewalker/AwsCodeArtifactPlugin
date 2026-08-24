import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "14.2.0"
    `kotlin-dsl`
    id("groovy-gradle-plugin")
    id("com.vanniktech.maven.publish") version "0.37.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}

tasks.named("publish") {
    dependsOn("check")
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
}

dependencies {
    implementation("software.amazon.awssdk:codeartifact:2.20.44")

    testImplementation(platform("org.spockframework:spock-bom:2.4-groovy-4.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    plugins {
        create("codeArtifactPlugin") {
            id = "io.github.comradewalker.aws-ca.ca"
            displayName = "A settings plugin that configures AWS CA repository"
            description = "Configures AWS CA Maven repository as source for project plugins and project dependencies."
//            tags.set(["tags", "for", "your", "plugins"])
            implementationClass = "io.github.comradewalker.awsca.CaPlugin"
        }
        create("codeArtifactPublishPlugin") {
            id = "io.github.comradewalker.aws-ca.ca-publish"
            displayName = "A project plugin for publishing artifacts to AWS CA repository"
            description = "Configures AWS CA Maven repository for artifact publishing. Requires io.github.comradewalker.aws-ca.ca settings plugin to be applied and configured."
            implementationClass = "io.github.comradewalker.awsca.CaPublishPlugin"
        }
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    coordinates("io.github.comradewalker", "aws-ca", "1.1.0")

    pom {
        name.set("AWS CodeArtifact Plugin")
        description.set("Plugin that configures AWS CA Maven repository as source for project plugins and project dependencies and as repository for artifact publishing.")
        inceptionYear.set("2023")
        url.set("https://github.com/comradewalker/AwsCodeArtifactPlugin/")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("comradewalker")
                name.set("Comrade Walker")
                url.set("https://github.com/comradewalker/")
            }
        }
        scm {
            url.set("https://github.com/comradewalker/AwsCodeArtifactPlugin/")
            connection.set("scm:git:git://github.com/comradewalker/AwsCodeArtifactPlugin.git")
            developerConnection.set("scm:git:ssh://git@github.com/comradewalker/AwsCodeArtifactPlugin.git")
        }
    }
}

ktlint {
    android.set(false)
    outputToConsole.set(true)
    outputColorName.set("RED")
    reporters {
        reporter(ReporterType.PLAIN)
    }
}