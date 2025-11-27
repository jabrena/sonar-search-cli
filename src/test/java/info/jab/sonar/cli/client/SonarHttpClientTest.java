package info.jab.sonar.cli.client;

import info.jab.sonar.cli.model.Issue;
import info.jab.sonar.cli.model.Severity;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for SonarHttpClient using WireMock.
 */
@DisplayName("SonarHttpClient Tests")
class SonarHttpClientTest {

    private WireMockServer wireMockServer;
    private SonarHttpClient client;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(0);
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
        baseUrl = "http://localhost:" + wireMockServer.port();

        // Create a custom HttpClient that will use WireMock
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();
        client = new SonarHttpClient(httpClient);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    @DisplayName("should search bugs successfully")
    void searchIssues_bugs_returnsValidResponse() throws Exception {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.BUG;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        URI uri = URI.create(String.format("%s/api/issues/search?componentKeys=%s&types=%s", baseUrl, componentKey, typesParam));
        String result = client.get(uri, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 4")
            .contains("\"type\": \"BUG\"");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    @DisplayName("should search code smells successfully")
    void searchIssues_codeSmells_returnsValidResponse() throws Exception {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.CODE_SMELL;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("code-smells.json")));

        // When
        URI uri = URI.create(String.format("%s/api/issues/search?componentKeys=%s&types=%s", baseUrl, componentKey, typesParam));
        String result = client.get(uri, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 893")
            .contains("\"type\": \"CODE_SMELL\"");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    @DisplayName("should search vulnerabilities successfully")
    void searchIssues_vulnerabilities_returnsValidResponse() throws Exception {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.VULNERABILITY;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("vulnerability.json")));

        // When
        URI uri = URI.create(String.format("%s/api/issues/search?componentKeys=%s&types=%s", baseUrl, componentKey, typesParam));
        String result = client.get(uri, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 4")
            .contains("\"type\": \"VULNERABILITY\"");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    @DisplayName("should throw exception when HTTP error occurs")
    void searchIssues_httpError_throwsRuntimeException() {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.BUG;
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .willReturn(aResponse()
                .withStatus(401)
                .withBody("Unauthorized")));

        // When & Then
        URI uri = URI.create(String.format("%s/api/issues/search?componentKeys=%s&types=%s", baseUrl, componentKey, issueType.toApiFormat()));
        assertThatThrownBy(() -> client.get(uri, apiKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HTTP 401");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search")));
    }

    @Test
    @DisplayName("should search all issue types successfully")
    void searchIssues_allTypes_returnsValidResponse() throws Exception {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.ALL;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        URI uri = URI.create(String.format("%s/api/issues/search?componentKeys=%s&types=%s", baseUrl, componentKey, typesParam));
        String result = client.get(uri, apiKey);

        // Then
        assertThat(result).isNotNull();
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    @DisplayName("should search issues with severity filter successfully")
    void searchIssues_withSeverity_returnsValidResponse() throws Exception {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.BUG;
        Severity severity = Severity.BLOCKER;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();
        String severityParam = severity.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withQueryParam("severities", equalTo(severityParam))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        URI uri = URI.create(String.format("%s/api/issues/search?componentKeys=%s&types=%s&severities=%s", baseUrl, componentKey, typesParam, severityParam));
        String result = client.get(uri, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 4");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withQueryParam("severities", equalTo(severityParam)));
    }

    @Test
    @DisplayName("should get response with status code successfully")
    void getWithStatus_success_returnsValidResponse() throws Exception {
        // Given
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/authentication/validate"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")));

        // When
        URI uri = URI.create(String.format("%s/api/authentication/validate", baseUrl));
        SonarHttpClient.Response result = client.getWithStatus(uri, apiKey);

        // Then
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.body()).contains("\"valid\": true");
        verify(getRequestedFor(urlPathEqualTo("/api/authentication/validate")));
    }

    @Test
    @DisplayName("should return unauthorized status when API key is invalid")
    void getWithStatus_unauthorized_returns401Status() throws Exception {
        // Given
        String apiKey = "invalid-api-key";

        stubFor(get(urlPathEqualTo("/api/authentication/validate"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(401)
                .withBody("Unauthorized")));

        // When
        URI uri = URI.create(String.format("%s/api/authentication/validate", baseUrl));
        SonarHttpClient.Response result = client.getWithStatus(uri, apiKey);

        // Then
        assertThat(result.statusCode()).isEqualTo(401);
        assertThat(result.body()).isEqualTo("Unauthorized");
        verify(getRequestedFor(urlPathEqualTo("/api/authentication/validate")));
    }

    @Test
    @DisplayName("should search hotspots successfully")
    void getHotspots_validProjectKey_returnsValidResponse() throws Exception {
        // Given
        String projectKey = "jabrena_churrera-cli";
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/hotspots/search"))
            .withQueryParam("projectKey", equalTo(projectKey))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("hotspots.json")));

        // When
        URI uri = URI.create(String.format("%s/api/hotspots/search?projectKey=%s", baseUrl, projectKey));
        String result = client.get(uri, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 1")
            .contains("\"hotspots\"")
            .contains("\"securityCategory\": \"dos\"")
            .contains("\"vulnerabilityProbability\": \"MEDIUM\"");
        verify(getRequestedFor(urlPathEqualTo("/api/hotspots/search"))
            .withQueryParam("projectKey", equalTo(projectKey)));
    }

}
