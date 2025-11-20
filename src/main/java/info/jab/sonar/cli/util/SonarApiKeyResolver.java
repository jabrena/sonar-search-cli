package info.jab.sonar.cli.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Optional;

/**
 * Class for resolving API keys from various sources.
 * Uses functional approach with Optional and flatMap.
 */
public class SonarApiKeyResolver {

    /**
     * The name of the environment variable for the Cursor API key.
     */
    public static final String SONAR_TOKEN = "SONAR_TOKEN";

    /**
     * Resolves the API key from .env file or system environment using functional approach.
     * Priority: .env file > system environment variable
     *
     * @return The resolved API key
     * @throws IllegalArgumentException if no API key is found
     */
    public String resolveApiKey() {
        return resolveFromEnvFile()
            .or(() -> resolveFromSystemEnvironment())
            .orElseThrow(() -> new IllegalArgumentException(
                "API key not found. Please provide it via:\n" +
                "  1. .env file: " + SONAR_TOKEN + "=YOUR_API_KEY\n" +
                "  2. Environment variable: export " + SONAR_TOKEN + "=YOUR_API_KEY"
            ));
    }

    /**
     * Lambda that resolves API key from .env file.
     * Returns Optional.empty() if not found or if there's an error.
     */
    private Optional<String> resolveFromEnvFile() {
        try {
            Dotenv dotenv = Dotenv.configure()
                .directory(System.getProperty("user.dir"))
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

            String envApiKey = dotenv.get(SONAR_TOKEN);
            if (envApiKey != null && !envApiKey.trim().isEmpty()) {
                return Optional.of(envApiKey.trim());
            }
        } catch (Exception e) {
            System.err.println("⚠️  Could not read .env file: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Lambda that resolves API key from system environment variable.
     * Returns Optional.empty() if not found.
     */
    private Optional<String> resolveFromSystemEnvironment() {
        String systemPropertyValue = System.getProperty(SONAR_TOKEN);
        if (systemPropertyValue != null) {
            String trimmed = systemPropertyValue.trim();
            return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
        }

        return Optional.ofNullable(System.getenv(SONAR_TOKEN))
            .filter(key -> !key.trim().isEmpty())
            .map(String::trim);
    }

}
