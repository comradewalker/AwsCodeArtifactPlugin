package hr.rao.android.plugin.ca

import org.gradle.api.provider.Property

abstract class CodeArtifactPluginExtension {
    abstract Property<String> getDomain()
    abstract Property<String> getAccountId()
    abstract Property<String> getRegion()
    abstract Property<String> getRepo()
}
