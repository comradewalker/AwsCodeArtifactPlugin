package io.github.comradewalker.awsca

import io.github.comradewalker.awsca.ca.CodeArtifactRepoProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin

class CaPublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(MavenPublishPlugin::class.java)) {
            target.plugins.apply(MavenPublishPlugin::class.java)
        }

        target.afterEvaluate {
            val codeArtifactRepoProviderService = target.gradle.sharedServices.registrations.findByName("codeArtifactRepoProvider")
                ?: throw IllegalStateException("Please apply the io.github.comradewalker.aws-ca.ca plugin in the settings file first and configure the codeArtifact extension")

            val publishing = extensions.getByType(PublishingExtension::class.java)
            publishing.repositories {
                maven {
                    @Suppress("UNCHECKED_CAST")
                    (codeArtifactRepoProviderService.service as Provider<CodeArtifactRepoProvider>).get().configureRepo(this)
                }
            }
        }
    }
}