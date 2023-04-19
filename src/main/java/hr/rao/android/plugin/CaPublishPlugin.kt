package hr.rao.android.plugin

import hr.rao.android.plugin.ca.CodeArtifactRepoProvider
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
                ?: throw IllegalStateException("Please apply the hr.rao.android.plugin.ca plugin in the settings file first and configure the codeArtifact extension")

            val publishing = extensions.getByType(PublishingExtension::class.java)
            publishing.repositories {
                maven { (codeArtifactRepoProviderService.service as Provider<CodeArtifactRepoProvider>).get().configureRepo(this) }
            }
        }
    }
}