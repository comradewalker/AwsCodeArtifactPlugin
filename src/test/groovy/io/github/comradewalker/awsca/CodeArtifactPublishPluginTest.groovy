package io.github.comradewalker.awsca

import org.gradle.testkit.runner.TaskOutcome

class CodeArtifactPublishPluginTest extends PluginTest {

    def "fails with a helpful message when publishing plugin is applied without the settings plugin"() {
        given:
        buildFile << """
            plugins {
                id("java-library") 
                id("io.github.comradewalker.aws-ca.ca-publish") 
            }
            publishing {
                publications {
                    maven(MavenPublication) {
                        groupId = "com.foo"
                        artifactId = "bar"
                        version = "0.1"
                        from(components.java)
                    }
                }
            }
        """
        file("src/main/java/Foo.java") << "class Foo {}"

        when:
        def result = runTaskWithFailure("publish")

        then:
//        result.output.contains("Failed to apply plugin 'io.github.comradewalker.aws-ca.ca-publish'")
        result.output.contains("FAILURE: Build failed with an exception.")
        result.output.contains("Please apply the io.github.comradewalker.aws-ca.ca plugin in the settings file first and configure the codeArtifact extension")
    }

    def "attempts to publish to CodeArtifact repository"() {
        given:
        givenCodeArtifactWillReturnAuthToken()
        givenCodeArtifactPluginIsConfigured()
        buildFile << """
            plugins {
                id("java-library") 
                id("io.github.comradewalker.aws-ca.ca-publish") 
            }
            publishing {
                publications {
                    maven(MavenPublication) {
                        groupId = "com.foo"
                        artifactId = "bar"
                        version = "0.1"
                        from(components.java)
                    }
                }
            }
        """
        file("src/main/java/Foo.java") << "class Foo {}"

        when:
        def result = runTaskWithFailure("publish")

        then:

        println("TASK RESULTS:")
        println(result.tasks)
        result.task(":publishMavenPublicationToCodeArtifactRepository").outcome == TaskOutcome.FAILED
        result.output.contains("Failed to publish publication 'maven' to repository 'codeArtifact'")
        result.output.contains("Could not PUT '${wiremock.baseUrl()}/${domain}-${accountId}.d.codeartifact.${region}.amazonaws.com/maven/$repo/com/foo/bar/0.1/bar-0.1.jar'")
    }
}
