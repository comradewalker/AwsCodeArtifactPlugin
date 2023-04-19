package hr.rao.android.plugin

import hr.rao.android.plugin.ca.CodeArtifactPluginExtension
import hr.rao.android.plugin.ca.CodeArtifactRepoProvider
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.initialization.Settings
import org.gradle.api.plugins.ExtensionAware

class CaPlugin : Plugin<Settings> {
    override fun apply(target: Settings) {
        val codeArtifact = target.extensions.create("codeArtifact", CodeArtifactPluginExtension::class.java)
        val serviceProvider =
            target.gradle.sharedServices.registerIfAbsent("codeArtifactRepoProvider", CodeArtifactRepoProvider::class.java)
            {
                parameters.domain.set(codeArtifact.domain)
                parameters.accountId.set(codeArtifact.accountId)
                parameters.region.set(codeArtifact.region)
                parameters.repo.set(codeArtifact.repo)
            }
        target.gradle.settingsEvaluated {
            pluginManagement {
                repositories {
                    maven {
                        serviceProvider.get().configureRepo(this)
                    }
                }
            }
            dependencyResolutionManagement {
                repositories {
                    maven {
                        serviceProvider.get().configureRepo(this)
                    }
                }
            }
        }
    }
}

/* WORKAROUND: for some reason a type-safe accessor is not generated for the extension,
* even though it is present in the extension container where the plugin is applied.
* This seems to work fine, and the extension methods are only available when the plugin
* is actually applied. */

/**
 * Retrieves the [CodeArtifactPluginExtension] extension.
 */
val Settings.codeArtifact: CodeArtifactPluginExtension
    get() = (this as ExtensionAware).extensions.getByName("codeArtifact") as CodeArtifactPluginExtension

/**
 * Configures the [CodeArtifactPluginExtension] extension.
 */
fun Settings.codeArtifact(configure: Action<CodeArtifactPluginExtension>): Unit =
    (this as ExtensionAware).extensions.configure("codeArtifact", configure)