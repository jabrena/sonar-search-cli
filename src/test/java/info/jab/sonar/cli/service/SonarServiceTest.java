package info.jab.sonar.cli.service;

import info.jab.sonar.cli.client.SonarHttpClient;
import info.jab.sonar.cli.model.Issue;
import info.jab.sonar.cli.model.Severity;
import info.jab.sonar.cli.model.Status;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for SonarService using WireMock.
 */
@DisplayName("SonarService Tests")
class SonarServiceTest {

    private WireMockServer wireMockServer;
    private SonarService sonarService;
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
        SonarHttpClient sonarHttpClient = new SonarHttpClient(httpClient);
        sonarService = new SonarService(baseUrl, sonarHttpClient);
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
            .withQueryParam("ps", equalTo("100"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        String result = sonarService.searchIssues(componentKey, issueType, null, null, 100, apiKey);

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
            .withQueryParam("ps", equalTo("100"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("code-smells.json")));

        // When
        String result = sonarService.searchIssues(componentKey, issueType, null, null, 100, apiKey);

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
            .withQueryParam("ps", equalTo("100"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("vulnerability.json")));

        // When
        String result = sonarService.searchIssues(componentKey, issueType, null, null, 100, apiKey);

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
        assertThatThrownBy(() -> sonarService.searchIssues(componentKey, issueType, null, null, 100, apiKey))
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
            .withQueryParam("ps", equalTo("100"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        String result = sonarService.searchIssues(componentKey, issueType, null, null, 100, apiKey);

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
            .withQueryParam("ps", equalTo("100"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        String result = sonarService.searchIssues(componentKey, issueType, severity, null, 100, apiKey);

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
    @DisplayName("should search issues with status filter successfully")
    void searchIssues_withStatus_returnsValidResponse() throws Exception {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.BUG;
        Status status = Status.OPEN;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();
        String statusParam = status.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withQueryParam("statuses", equalTo(statusParam))
            .withQueryParam("ps", equalTo("100"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        String result = sonarService.searchIssues(componentKey, issueType, null, status, 100, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 4");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withQueryParam("statuses", equalTo(statusParam)));
    }

    @Test
    @DisplayName("should search issues with custom page size successfully")
    void searchIssues_withPageSize_returnsValidResponse() throws Exception {
        // Given
        String componentKey = "jabrena_churrera-cli";
        Issue issueType = Issue.BUG;
        int pageSize = 200;
        String apiKey = "test-api-key";
        String typesParam = issueType.toApiFormat();

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withQueryParam("ps", equalTo("200"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        String result = sonarService.searchIssues(componentKey, issueType, null, null, pageSize, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 4");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("componentKeys", equalTo(componentKey))
            .withQueryParam("types", equalTo(typesParam))
            .withQueryParam("ps", equalTo("200")));
    }

    @Test
    @DisplayName("should validate token successfully when token is valid")
    void validateToken_validToken_returnsTrue() throws Exception {
        // Given
        String apiKey = "valid-api-key";

        stubFor(get(urlPathEqualTo("/api/authentication/validate"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("{\"valid\": true}")));

        // When
        boolean result = sonarService.validateToken(apiKey);

        // Then
        assertThat(result).isTrue();
        verify(getRequestedFor(urlPathEqualTo("/api/authentication/validate")));
    }

    @Test
    @DisplayName("should return false when token is invalid")
    void validateToken_invalidToken_returnsFalse() throws Exception {
        // Given
        String apiKey = "invalid-api-key";

        stubFor(get(urlPathEqualTo("/api/authentication/validate"))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(401)
                .withBody("Unauthorized")));

        // When
        boolean result = sonarService.validateToken(apiKey);

        // Then
        assertThat(result).isFalse();
        verify(getRequestedFor(urlPathEqualTo("/api/authentication/validate")));
    }

    @Test
    @DisplayName("should search hotspots successfully")
    void searchHotspots_validProjectKey_returnsValidResponse() throws Exception {
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
        String result = sonarService.searchHotspots(projectKey, apiKey);

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

    @Test
    @DisplayName("should search issue detail successfully")
    void searchIssueDetail_validIssueKey_returnsValidResponse() throws Exception {
        // Given
        String issueKey = "AZqZJmQWWyUHIeVsO2He";
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("issues", equalTo(issueKey))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("bugs.json")));

        // When
        String result = sonarService.searchIssueDetail(issueKey, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 4")
            .contains("\"type\": \"BUG\"");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("issues", equalTo(issueKey)));
    }

    @Test
    @DisplayName("should throw exception when issue detail search returns HTTP error")
    void searchIssueDetail_httpError_throwsRuntimeException() {
        // Given
        String issueKey = "AZqZJmQWWyUHIeVsO2He";
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("issues", equalTo(issueKey))
            .willReturn(aResponse()
                .withStatus(404)
                .withBody("Issue not found")));

        // When & Then
        assertThatThrownBy(() -> sonarService.searchIssueDetail(issueKey, apiKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HTTP 404");
        verify(getRequestedFor(urlPathEqualTo("/api/issues/search"))
            .withQueryParam("issues", equalTo(issueKey)));
    }

    @Test
    @DisplayName("should search hotspot detail successfully")
    void searchHotspotDetail_validHotspotKey_returnsValidResponse() throws Exception {
        // Given
        String hotspotKey = "AXqZJmQWWyUHIeVsO2Hf";
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/hotspots/show"))
            .withQueryParam("key", equalTo(hotspotKey))
            .withHeader("Authorization", equalTo("Bearer " + apiKey))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBodyFile("hotspots.json")));

        // When
        String result = sonarService.searchHotspotDetail(hotspotKey, apiKey);

        // Then
        assertThat(result)
            .isNotNull()
            .contains("\"total\": 1")
            .contains("\"hotspots\"");
        verify(getRequestedFor(urlPathEqualTo("/api/hotspots/show"))
            .withQueryParam("key", equalTo(hotspotKey)));
    }

    @Test
    @DisplayName("should throw exception when hotspot detail search returns HTTP error")
    void searchHotspotDetail_httpError_throwsRuntimeException() {
        // Given
        String hotspotKey = "AXqZJmQWWyUHIeVsO2Hf";
        String apiKey = "test-api-key";

        stubFor(get(urlPathEqualTo("/api/hotspots/show"))
            .withQueryParam("key", equalTo(hotspotKey))
            .willReturn(aResponse()
                .withStatus(404)
                .withBody("Hotspot not found")));

        // When & Then
        assertThatThrownBy(() -> sonarService.searchHotspotDetail(hotspotKey, apiKey))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("HTTP 404");
        verify(getRequestedFor(urlPathEqualTo("/api/hotspots/show"))
            .withQueryParam("key", equalTo(hotspotKey)));
    }
}

