package info.jab.sonar.cli.client;

import info.jab.sonar.cli.model.IssueType;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for SonarHttpClient using WireMock.
 */
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
        client = new SonarHttpClient(httpClient, baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Test
    void testSearchIssues_Bugs() throws Exception {
        // Arrange
        String componentKey = "jabrena_churrera-cli";
        IssueType issueType = IssueType.BUG;
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

        // Act
        String result = client.getIssues(apiKey, componentKey, issueType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("\"total\": 4");
        assertThat(result).contains("\"type\": \"BUG\"");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    void testSearchIssues_CodeSmells() throws Exception {
        // Arrange
        String componentKey = "jabrena_churrera-cli";
        IssueType issueType = IssueType.CODE_SMELL;
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

        // Act
        String result = client.getIssues(apiKey, componentKey, issueType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("\"total\": 893");
        assertThat(result).contains("\"type\": \"CODE_SMELL\"");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    void testSearchIssues_Vulnerabilities() throws Exception {
        // Arrange
        String componentKey = "jabrena_churrera-cli";
        IssueType issueType = IssueType.VULNERABILITY;
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

        // Act
        String result = client.getIssues(apiKey, componentKey, issueType);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("\"total\": 4");
        assertThat(result).contains("\"type\": \"VULNERABILITY\"");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    void testSearchIssues_HttpError() {
        // Arrange
        String componentKey = "jabrena_churrera-cli";
        IssueType issueType = IssueType.BUG;
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .willReturn(aResponse()
                .withStatus(401)
                .withBody("Unauthorized")));

        // Act & Assert
        assertThatThrownBy(() -> client.getIssues(apiKey, componentKey, issueType))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HTTP 401");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search")));
    }

    @Test
    void testSearchIssues_AllTypes() throws Exception {
        // Arrange
        String componentKey = "jabrena_churrera-cli";
        IssueType issueType = IssueType.ALL;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // Act
        String result = client.getIssues(apiKey, componentKey, issueType);

        // Assert
        assertThat(result).isNotNull();
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("types", equalTo(typesParam)));
    }

    @Test
    void testValidateToken_Valid() throws Exception {
        String apiKey = "valid-api-key";

        stubFor(get(urlPathEqualTo("/api/authentication/validate"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")));

        boolean result = client.validateToken(apiKey);

        assertThat(result).isTrue();
        verify(getRequestedFor(urlPathEqualTo("/api/authentication/validate")));
    }

    @Test
    void testValidateToken_Invalid() throws Exception {
        String apiKey = "invalid-api-key";

        stubFor(get(urlPathEqualTo("/api/authentication/validate"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(401)
                .withBody("Unauthorized")));

        boolean result = client.validateToken(apiKey);

        assertThat(result).isFalse();
        verify(getRequestedFor(urlPathEqualTo("/api/authentication/validate")));
    }

    @Test
    void testGetHotspots() throws Exception {
        // Arrange
        String projectKey = "jabrena_churrera-cli";
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/hotspots/search"))
            .withQueryParam("projectKey", equalTo(projectKey))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("hotspots.json")));

        // Act
        String result = client.getHotspots(apiKey, projectKey);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).contains("\"total\": 1");
        assertThat(result).contains("\"hotspots\"");
        assertThat(result).contains("\"securityCategory\": \"dos\"");
        assertThat(result).contains("\"vulnerabilityProbability\": \"MEDIUM\"");
        verify(getRequestedFor(urlPathEqualTo("/api/hotspots/search"))
            .withQueryParam("projectKey", equalTo(projectKey)));
    }

}
