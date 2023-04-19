package io.github.comradewalker.awsca.ca

import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenResponse
import java.net.URI
import java.time.Instant
import java.util.function.Consumer

abstract class CodeArtifactRepoProvider : BuildService<CodeArtifactRepoProvider.Params> {
    fun configureRepo(spec: MavenArtifactRepository) {
        val params = parameters
        val domain = getRequiredProperty(params.domain)
        val accountId = getRequiredProperty(params.accountId)
        val region = getRequiredProperty(params.region)
        val repo = getRequiredProperty(params.repo)
        spec.name = "codeArtifact"
        configureCodeArtifactUrl(spec, domain, accountId, region, repo)
        spec.credentials {
            username = "aws"
            password = getToken(domain, accountId, region)
        }
    }

    fun getToken(domain: String, accountId: String, region: String): String {
        if (token == null || token!!.expiration() <= Instant.now()) {
            token = makeCodeArtifactClient(region).getAuthorizationToken(Consumer { builder: GetAuthorizationTokenRequest.Builder -> builder.domain(domain).domainOwner(accountId) })
        }
        return token?.authorizationToken() ?: ""
    }

    private var token: GetAuthorizationTokenResponse? = null

    interface Params : BuildServiceParameters {
        val domain: Property<String>
        val accountId: Property<String>
        val region: Property<String>
        val repo: Property<String>
    }

    companion object {
        private fun configureCodeArtifactUrl(spec: MavenArtifactRepository, domain: String, accountId: String, region: String, repo: String) {
            val overriddenCodeArtifactUrl = getOverriddenCodeArtifactUrl()
            spec.isAllowInsecureProtocol = !overriddenCodeArtifactUrl.isNullOrEmpty()
            val urlPrefix = overriddenCodeArtifactUrl ?: "https:/"
            spec.url = URI.create("$urlPrefix/$domain-$accountId.d.codeartifact.$region.amazonaws.com/maven/$repo/")
        }

        private fun makeCodeArtifactClient(region: String): CodeartifactClient {
            var client = CodeartifactClient.builder()
            val awsUrlOverride = getOverriddenCodeArtifactUrl()
            if (awsUrlOverride != null) {
                client = client.endpointOverride(URI.create(awsUrlOverride))
            }
            return client.region(Region.of(region)).build()
        }

        private fun getOverriddenCodeArtifactUrl(): String? = System.getenv("CODEARTIFACT_URL_OVERRIDE")

        private fun getRequiredProperty(property: Property<String>): String {
            check(property.isPresent) {
                """Please configure the AWS CodeArtifactRepository using the codeArtifact block in the settings file:
        
    GROOVY:
        
    codeArtifact {
        domain = "repo-domain"
        accountId = "123456789012"
        region = "eu-central-1"
        repo = "repo-name"
    }
    
    KTS:
        
    codeArtifact {
        domain.set("repo-domain")
        accountId.set("123456789012")
        region.set("eu-central-1")
        repo.set("repo-name")
    }
    
    or
        
    configure<io.github.comradewalker.awsca.ca.CodeArtifactPluginExtension>{
        domain.set("repo-domain")
        accountId.set("123456789012")
        region.set("eu-central-1")
        repo.set("repo-name")
    }
    """
            }
            return property.get()
        }
    }
}