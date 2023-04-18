package hr.rao.android.plugin;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.services.BuildServiceRegistration;

import hr.rao.android.plugin.ca.CodeArtifactRepoProvider;

public class CaPublishPlugin implements Plugin<Project>
{
	@Override
	public void apply(Project target)
	{

		if (!target.getPlugins().hasPlugin(MavenPublishPlugin.class))
		{
			target.getPlugins().apply(MavenPublishPlugin.class);
		}
//		target.getPluginManager().apply(MavenPublishPlugin.class);


		PublishingExtension publishing = target.getExtensions().getByType(PublishingExtension.class);

//		target.getExtensions().configure(PublishingExtension.class, ext -> {
//
//
//		});


		target.afterEvaluate(afterEvaluate -> {
			BuildServiceRegistration<?, ?> codeArtifactRepoProviderService = target.getGradle().getSharedServices().getRegistrations().findByName("codeArtifactRepoProvider");
			if (codeArtifactRepoProviderService == null)
			{
				throw new IllegalStateException("Please apply the hr.rao.android.plugin.ca plugin in the settings file first and configure the codeArtifact extension");
			}

			publishing.repositories(artifactRepositories -> {
				artifactRepositories.maven(((Provider<CodeArtifactRepoProvider>) codeArtifactRepoProviderService.getService()).get()::configureRepo);
			});

//			System.err.println("Applied ca publish plugin 1");
		});


//		System.err.println("Applied ca publish plugin 2");
	}
}
