import com.vanniktech.maven.publish.SonatypeHost

plugins {
    `kotlin-dsl`
    id("groovy-gradle-plugin")
    id("com.vanniktech.maven.publish") version "0.25.2"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withSourcesJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
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

    testImplementation(platform("org.spockframework:spock-bom:2.4-M1-groovy-3.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")
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
    publishToMavenCentral(SonatypeHost.S01)

    signAllPublications()

    coordinates("io.github.comradewalker", "aws-ca", "1.0.1-SNAPSHOT")

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
