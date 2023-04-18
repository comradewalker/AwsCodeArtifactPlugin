package hr.rao.android.plugin

class CodeArtifactPluginTest extends PluginTest {

    def "not configuring the codeartifact repository emits a helpful error message"() {
        settingsFile.setText("""
            plugins {
                id("hr.rao.android.plugin.ca")
            }
            $configuration
            ${settingsFile.text}
        """)
        def result = runTaskWithFailure("tasks")

        expect:
        result.output.contains("Please configure the AWS CodeArtifactRepository using the codeArtifact block in the settings file:")

        where:
        configuration << [
                "",
                "codeArtifact { domain = 'foo' }",
                "codeArtifact { domain = 'foo'; accountId = 'foo' }",
                "codeArtifact { domain = 'foo'; accountId = 'foo'; region = 'foo' }"
        ]
    }

    def "searches for plugins in configured CodeArtifact repository"() {
        given:
        givenCodeArtifactWillReturnAuthToken()
        givenCodeArtifactPluginIsConfigured()
        buildFile << """
            plugins {
                id("foo.bar").version("42")
            }
        """

        when:
        def result = runTaskWithFailure("tasks")

        then:
        result.output.contains("Plugin [id: 'foo.bar', version: '42'] was not found in any of the following sources:")
        result.output.contains("Searched in the following repositories:")
        result.output.contains("codeArtifact(${wiremock.baseUrl()}/${domain}-${accountId}.d.codeartifact.${region}.amazonaws.com/maven/$repo/)")
    }

    def "searches for dependencies in configured CodeArtifact repository"() {
        given:
        givenCodeArtifactWillReturnAuthToken()
        givenCodeArtifactPluginIsConfigured()
        buildFile << """
            plugins {
                id("java") 
            }
            dependencies {
                implementation("foo:bar:42")
            }
        """
        file("src/main/java/Foo.java") << "class Foo {}"

        when:
        def result = runTaskWithFailure("build")

        then:

        //for gradle-7.5
        result.output.contains("No such host is known")
        //for gradle-8.0.1 (AS Flamingo)
//        result.output.contains("Could not find foo:bar:42")
//        result.output.contains("Searched in the following locations")
//        result.output.contains("- ${wiremock.baseUrl()}/${domain}-${accountId}.d.codeartifact.${region}.amazonaws.com/maven/$repo/foo/bar/42/bar-42.pom")
    }
}
