package info.jab.sonar.cli.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.jab.sonar.cli.client.SonarHttpClient;
import info.jab.sonar.cli.model.Issue;
import info.jab.sonar.cli.model.Severity;
import info.jab.sonar.cli.model.Status;
import java.net.URI;

/**
 * Service for interacting with SonarCloud API.
 * Handles both URL building and HTTP communication.
 */
public class SonarService {

    private static final String SONARCLOUD_BASE_URL = "https://sonarcloud.io";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final String baseUrl;
    private final SonarHttpClient httpClient;

    public SonarService() {
        this.baseUrl = SONARCLOUD_BASE_URL;
        this.httpClient = new SonarHttpClient();
    }

    /**
     * Constructor for testing that allows specifying a custom base URL and HTTP client.
     *
     * @param baseUrl The base URL for the API (e.g., http://localhost:8080 for WireMock)
     * @param httpClient The HTTP client to use for requests
     */
    public SonarService(String baseUrl, SonarHttpClient httpClient) {
        this.baseUrl = baseUrl;
        this.httpClient = httpClient;
    }

    /**
     * Searches for issues in SonarCloud.
     *
     * @param componentKey The component key to search for
     * @param issueType The issue type enum (BUG, CODE_SMELL, VULNERABILITY, or ALL)
     * @param severity The optional severity enum (BLOCKER, CRITICAL, MAJOR, MINOR, INFO)
     * @param status The optional status enum (OPEN, CLOSED, CONFIRMED, REOPENED, RESOLVED)
     * @param pageSize The page size (number of results per page). Valid range: 1-500. Default: 100
     * @param apiKey The SonarCloud API key
     * @return The JSON response body as a string
     * @throws Exception if the request fails or returns a non-200 status code
     */
    public String searchIssues(String componentKey, Issue issueType, Severity severity, Status status, int pageSize, String apiKey) throws Exception {
        URI uri = buildIssuesSearchUrl(componentKey, issueType, severity, status, pageSize);
        return httpClient.get(uri, apiKey);
    }

    /**
     * Searches for security hotspots in SonarCloud.
     *
     * @param projectKey The project key to search for
     * @param apiKey The SonarCloud API key
     * @return The JSON response body as a string
     * @throws Exception if the request fails or returns a non-200 status code
     */
    public String searchHotspots(String projectKey, String apiKey) throws Exception {
        URI uri = buildHotspotsSearchUrl(projectKey);
        return httpClient.get(uri, apiKey);
    }

    /**
     * Searches for a specific issue by its key-id in SonarCloud.
     *
     * @param issueKey The issue key-id to search for
     * @param apiKey The SonarCloud API key
     * @return The JSON response body as a string
     * @throws Exception if the request fails or returns a non-200 status code
     */
    public String searchIssueDetail(String issueKey, String apiKey) throws Exception {
        URI uri = buildIssueDetailUrl(issueKey);
        return httpClient.get(uri, apiKey);
    }

    /**
     * Searches for a specific security hotspot by its key in SonarCloud.
     *
     * @param hotspotKey The hotspot key to search for
     * @param apiKey The SonarCloud API key
     * @return The JSON response body as a string
     * @throws Exception if the request fails or returns a non-200 status code
     */
    public String searchHotspotDetail(String hotspotKey, String apiKey) throws Exception {
        URI uri = buildHotspotDetailUrl(hotspotKey);
        return httpClient.get(uri, apiKey);
    }

    /**
     * Validates the provided SonarCloud token.
     *
     * @param apiKey The SonarCloud API key
     * @return true if the token is valid, false otherwise
     * @throws Exception if the request fails for unexpected reasons
     */
    public boolean validateToken(String apiKey) throws Exception {
        URI uri = buildTokenValidationUrl();
        SonarHttpClient.Response response = httpClient.getWithStatus(uri, apiKey);

        int statusCode = response.statusCode();
        String body = response.body();

        if (statusCode == 200) {
            return parseValidationResponse(body, true);
        }
        if (statusCode == 401) {
            return parseValidationResponse(body, false);
        }

        throw new RuntimeException(
            String.format("Error validating token: HTTP %d - %s", statusCode, body)
        );
    }

    private boolean parseValidationResponse(String responseBody, boolean defaultValue) {
        if (responseBody == null || responseBody.isBlank()) {
            return defaultValue;
        }

        try {
            ValidationResponse validationResponse =
                OBJECT_MAPPER.readValue(responseBody, ValidationResponse.class);
            Boolean valid = validationResponse.valid();
            return valid != null ? valid : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private record ValidationResponse(@JsonProperty("valid") Boolean valid) {
    }

    /**
     * Builds the URL for searching issues in SonarCloud.
     *
     * @param componentKey The component key to search for
     * @param issueType The issue type enum (BUG, CODE_SMELL, VULNERABILITY, or ALL)
     * @param severity The optional severity enum (BLOCKER, CRITICAL, MAJOR, MINOR, INFO)
     * @param status The optional status enum (OPEN, CLOSED, CONFIRMED, REOPENED, RESOLVED)
     * @param pageSize The page size (number of results per page). Valid range: 1-500
     * @return The complete URI for the issues search endpoint
     */
    private URI buildIssuesSearchUrl(String componentKey, Issue issueType, Severity severity, Status status, int pageSize) {
        String types = issueType.toApiFormat();
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(String.format("%s/api/issues/search?componentKeys=%s&types=%s",
            baseUrl, componentKey, types));

        if (severity != null) {
            urlBuilder.append("&severities=").append(severity.toApiFormat());
        }

        if (status != null) {
            urlBuilder.append("&statuses=").append(status.toApiFormat());
        }

        // Add page size parameter (ps)
        urlBuilder.append("&ps=").append(pageSize);

        return URI.create(urlBuilder.toString());
    }

    /**
     * Builds the URL for searching security hotspots in SonarCloud.
     *
     * @param projectKey The project key to search for
     * @return The complete URI for the hotspots search endpoint
     */
    private URI buildHotspotsSearchUrl(String projectKey) {
        String url = String.format("%s/api/hotspots/search?projectKey=%s",
            baseUrl, projectKey);
        return URI.create(url);
    }

    /**
     * Builds the URL for validating a SonarCloud token.
     *
     * @return The complete URI for the authentication validation endpoint
     */
    private URI buildTokenValidationUrl() {
        String url = String.format("%s/api/authentication/validate", baseUrl);
        return URI.create(url);
    }

    /**
     * Builds the URL for searching a specific issue by key-id in SonarCloud.
     *
     * @param issueKey The issue key-id to search for
     * @return The complete URI for the issue detail endpoint
     */
    private URI buildIssueDetailUrl(String issueKey) {
        String url = String.format("%s/api/issues/search?issues=%s",
            baseUrl, issueKey);
        return URI.create(url);
    }

    /**
     * Builds the URL for searching a specific security hotspot by key in SonarCloud.
     *
     * @param hotspotKey The hotspot key to search for
     * @return The complete URI for the hotspot detail endpoint
     */
    private URI buildHotspotDetailUrl(String hotspotKey) {
        String url = String.format("%s/api/hotspots/show?key=%s",
            baseUrl, hotspotKey);
        return URI.create(url);
    }
}

