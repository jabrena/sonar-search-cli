package info.jab.sonar.cli.client;

import info.jab.sonar.cli.model.IssueType;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for interacting with SonarCloud API.
 */
public class SonarHttpClient {

    private static final String SONARCLOUD_BASE_URL = "https://sonarcloud.io";
    private final HttpClient httpClient;
    private final String baseUrl;

    public SonarHttpClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.baseUrl = SONARCLOUD_BASE_URL;
    }

    /**
     * Constructor for testing that allows specifying a custom base URL.
     *
     * @param httpClient The HTTP client to use
     * @param baseUrl The base URL for the API (e.g., http://localhost:8080 for WireMock)
     */
    public SonarHttpClient(HttpClient httpClient, String baseUrl) {
        this.httpClient = httpClient;
        this.baseUrl = baseUrl;
    }

    /**
     * Searches for issues in SonarCloud.
     *
     * @param apiKey The SonarCloud API key
     * @param componentKey The component key to search for
     * @param issueType The issue type enum (BUG, CODE_SMELL, VULNERABILITY, or ALL)
     * @return The JSON response body as a string
     * @throws Exception if the request fails or returns a non-200 status code
     */
    public String searchIssues(String apiKey, String componentKey, IssueType issueType) throws Exception {
        String types = issueType.toApiFormat();
        String url = String.format("%s/api/issues/search?componentKeys=%s&types=%s",
            baseUrl, componentKey, types);

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "Bearer " + apiKey)
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            throw new RuntimeException(
                String.format("Error: HTTP %d - %s", response.statusCode(), response.body())
            );
        }
    }
}

