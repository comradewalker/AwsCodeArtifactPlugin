package hr.rao.android.plugin.ca

import org.gradle.api.provider.Property

abstract class CodeArtifactPluginExtension {
    abstract val domain: Property<String>
    abstract val accountId: Property<String>
    abstract val region: Property<String>
    abstract val repo: Property<String>
}