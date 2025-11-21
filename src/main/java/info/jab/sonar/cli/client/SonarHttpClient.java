package info.jab.sonar.cli.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * HTTP client for sending requests to SonarCloud API.
 * This class is responsible only for HTTP communication.
 */
public class SonarHttpClient {

    private final HttpClient httpClient;

    public SonarHttpClient() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    /**
     * Constructor for testing that allows specifying a custom HTTP client.
     *
     * @param httpClient The HTTP client to use
     */
    public SonarHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Sends a GET request to the specified URI with the provided API key.
     *
     * @param uri The URI to send the request to
     * @param apiKey The SonarCloud API key
     * @return The JSON response body as a string
     * @throws Exception if the request fails or returns a non-200 status code
     */
    public String get(URI uri, String apiKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
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

    /**
     * Sends a GET request and returns the response with status code and body.
     * This method does not throw exceptions for non-200 status codes.
     *
     * @param uri The URI to send the request to
     * @param apiKey The SonarCloud API key
     * @return A Response object containing status code and body
     * @throws Exception if the request fails for network/connection reasons
     */
    public Response getWithStatus(URI uri, String apiKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .header("Authorization", "Bearer " + apiKey)
            .GET()
            .timeout(Duration.ofSeconds(30))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return new Response(response.statusCode(), response.body());
    }

    /**
     * Response object containing HTTP status code and body.
     */
    public record Response(int statusCode, String body) {
    }
}

