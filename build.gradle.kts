plugins {
    `kotlin-dsl`
    id("groovy-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.2.0"
}

group = "hr.rao.android.plugin"
version = "1.0.1"

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
    website.set("https://www.rao.hr")
    vcsUrl.set("https://www.rao.hr")
    plugins {
        create("codeArtifactPlugin") {
            id = "hr.rao.android.plugin.ca"
            displayName = "A settings plugin (AGP) that configures AWS CA repository"
            description = "Configures AWS CA Maven repository as source for project (AGP) plugins and project dependencies."
//            tags.set(["tags", "for", "your", "plugins"])
            implementationClass = "hr.rao.android.plugin.CaPlugin"
        }
        create("codeArtifactPublishPlugin") {
            id = "hr.rao.android.plugin.ca-publish"
            displayName = "A project plugin (AGP) for publishing artifacts to AWS CA repository"
            description = "Configures AWS CA Maven repository for artifact publishing. Requires hr.rao.android.plugin.ca settings plugin to be applied and configured."
            implementationClass = "hr.rao.android.plugin.CaPublishPlugin"
        }
    }
}
