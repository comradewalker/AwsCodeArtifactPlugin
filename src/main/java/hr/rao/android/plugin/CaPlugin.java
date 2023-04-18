package hr.rao.android.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.provider.Provider;

import hr.rao.android.plugin.ca.CodeArtifactPluginExtension;
import hr.rao.android.plugin.ca.CodeArtifactRepoProvider;

public class CaPlugin implements Plugin<Settings>
{
	@Override
	public void apply(Settings target)
	{
		CodeArtifactPluginExtension codeArtifact = target.getExtensions().create("codeArtifact", CodeArtifactPluginExtension.class);
		Provider<CodeArtifactRepoProvider> serviceProvider = target.getGradle().getSharedServices().registerIfAbsent("codeArtifactRepoProvider", CodeArtifactRepoProvider.class, it -> {
			it.getParameters().getDomain().set(codeArtifact.getDomain());
			it.getParameters().getAccountId().set(codeArtifact.getAccountId());
			it.getParameters().getRegion().set(codeArtifact.getRegion());
			it.getParameters().getRepo().set(codeArtifact.getRepo());
		});


		target.getGradle().settingsEvaluated(settings -> {
			settings.pluginManagement(pluginManagementSpec -> {
				pluginManagementSpec.repositories(artifactRepositories -> {
					artifactRepositories.maven(serviceProvider.get()::configureRepo);
				});
			});

			settings.dependencyResolutionManagement(dependencyResolutionManagement -> {
				dependencyResolutionManagement.repositories(artifactRepositories -> {
					artifactRepositories.maven(serviceProvider.get()::configureRepo);
				});
			});
		});
//		System.err.println("Applied ca settings plugin");
	}
}
