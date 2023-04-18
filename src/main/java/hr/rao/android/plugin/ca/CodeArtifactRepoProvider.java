package hr.rao.android.plugin.ca;

import java.net.URI;
import java.time.Instant;

import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.provider.Property;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.codeartifact.CodeartifactClient;
import software.amazon.awssdk.services.codeartifact.CodeartifactClientBuilder;
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenResponse;

public abstract class CodeArtifactRepoProvider implements BuildService<CodeArtifactRepoProvider.Params>
{
	public void configureRepo(MavenArtifactRepository spec)
	{
		Params params = getParameters();
		final String domain = getRequiredProperty(params.getDomain());
		final String accountId = getRequiredProperty(params.getAccountId());
		final String region = getRequiredProperty(params.getRegion());
		String repo = getRequiredProperty(params.getRepo());

		spec.setName("codeArtifact");
		configureCodeArtifactUrl(spec, domain, accountId, region, repo);
		spec.credentials(passwordCredentials -> {
			passwordCredentials.setUsername("aws");
			passwordCredentials.setPassword(getToken(domain, accountId, region));
		});
	}

	public String getToken(String domain, String accountId, String region)
	{
		if (token == null || token.expiration().compareTo(Instant.now()) <= 0)
		{
			token = makeCodeArtifactClient(region).getAuthorizationToken(builder -> {
				builder.domain(domain).domainOwner(accountId);
			});
		}

		return token.authorizationToken();
	}

	private static void configureCodeArtifactUrl(MavenArtifactRepository spec, final String domain, final String accountId, final String region, final String repo)
	{
		String overriddenCodeArtifactUrl = getOverriddenCodeArtifactUrl();
		spec.setAllowInsecureProtocol(overriddenCodeArtifactUrl != null);
		final String urlPrefix = overriddenCodeArtifactUrl == null ? "https:/" : overriddenCodeArtifactUrl;
		spec.setUrl(URI.create(urlPrefix + "/" + domain + "-" + accountId + ".d.codeartifact." + region + ".amazonaws.com/maven/" + repo));
	}

	private static CodeartifactClient makeCodeArtifactClient(String region)
	{
		CodeartifactClientBuilder client = CodeartifactClient.builder();
		String awsUrlOverride = getOverriddenCodeArtifactUrl();
		if (awsUrlOverride != null)
		{
			client = client.endpointOverride(URI.create(awsUrlOverride));
		}

		return client.region(Region.of(region)).build();
	}

	private static String getOverriddenCodeArtifactUrl()
	{
		return System.getenv("CODEARTIFACT_URL_OVERRIDE");
	}

	private static String getRequiredProperty(Property<String> property)
	{
		if (!property.isPresent())
		{
			throw new IllegalStateException("Please configure the AWS CodeArtifactRepository using the codeArtifact block in the settings file:\n    codeArtifact {\n        domain = \"repo - domain\"\n        accountId = \"123456789012\"\n        region = \"us - east - 1\"\n        repo = \"repo - name\"\n    }");
		}

		return property.get();
	}

	private GetAuthorizationTokenResponse token = null;

	public interface Params extends BuildServiceParameters
	{
		Property<String> getDomain();

		Property<String> getAccountId();

		Property<String> getRegion();

		Property<String> getRepo();
	}
}
