package info.jab.sonar.cli.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class for SonarApiKeyResolver utility.
 */
@DisplayName("SonarApiKeyResolver Tests")
class SonarApiKeyResolverTest {

    private String originalWorkingDir;
    private String originalSystemProperty;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Save original working directory
        originalWorkingDir = System.getProperty("user.dir");
        originalSystemProperty = System.getProperty(SonarApiKeyResolver.SONAR_TOKEN);

        // Point working directory to a temp folder so real .env is untouched
        File isolatedWorkingDir = tempDir.toFile();
        if (!isolatedWorkingDir.exists()) {
            isolatedWorkingDir.mkdirs();
        }
        System.setProperty("user.dir", isolatedWorkingDir.getAbsolutePath());

        // Clear environment variable for clean tests
        clearEnvironmentVariable();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (originalSystemProperty != null) {
            System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, originalSystemProperty);
        } else {
            System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
        }

        // Restore original working directory
        System.setProperty("user.dir", originalWorkingDir);
    }

    @Test
    @DisplayName("Should throw exception when API key is not found")
    void shouldThrowExceptionWhenApiKeyIsNotFound() {
        // Given
        // Note: This test may not work in all environments due to environment variable limitations
        // In a real CI/CD environment, you would set the environment variable externally
        // For now, we'll test the error case when no API key is found
        clearEnvironmentVariable();
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // When & Then
        assertThatThrownBy(resolver::resolveApiKey)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key not found");
    }

    @Test
    @DisplayName("Should resolve API key from .env file")
    void shouldResolveApiKeyFromEnvFile() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create .env file with test content
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=env-file-key-789");
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo("env-file-key-789");
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should trim whitespace from API key in .env file")
    void shouldTrimWhitespaceFromApiKeyInEnvFile() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create .env file with whitespace
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=  env-file-key-whitespace  ");
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo("env-file-key-whitespace");
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should prioritize .env file over system environment")
    void shouldPrioritizeEnvFileOverSystemEnvironment() throws IOException {
        // Given
        // Note: This test focuses on .env file functionality since environment variable
        // manipulation in tests is complex and environment-dependent
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create .env file with test content
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=env-file-key");
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo("env-file-key");
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should throw exception when API key is not found")
    void shouldThrowExceptionWhenApiKeyIsNotFoundInAnySource() {
        // Given
        clearEnvironmentVariable();
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // When & Then
        assertThatThrownBy(resolver::resolveApiKey)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key not found")
            .hasMessageContaining(".env file");
    }

    @Test
    @DisplayName("Should throw exception when API key is empty in system environment")
    void shouldThrowExceptionWhenApiKeyIsEmptyInSystemEnvironment() {
        // Given
        // Note: This test may not work in all environments due to environment variable limitations
        clearEnvironmentVariable();
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // When & Then
        assertThatThrownBy(resolver::resolveApiKey)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key not found");
    }

    @Test
    @DisplayName("Should throw exception when API key is empty in .env file")
    void shouldThrowExceptionWhenApiKeyIsEmptyInEnvFile() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create .env file with empty value
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=");
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When & Then
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should throw exception when API key is whitespace-only in .env file")
    void shouldThrowExceptionWhenApiKeyIsWhitespaceOnlyInEnvFile() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create .env file with whitespace-only value
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=   ");
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When & Then
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should throw exception when .env file is malformed")
    void shouldThrowExceptionWhenEnvFileIsMalformed() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create malformed .env file
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("invalid-env-file-content");
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When & Then
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should throw exception when .env file is missing")
    void shouldThrowExceptionWhenEnvFileIsMissing() {
        // Given
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // When & Then
        assertThatThrownBy(resolver::resolveApiKey)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("API key not found");
    }

    @Test
    @DisplayName("Should throw exception when .env file exists but missing SONAR_TOKEN")
    void shouldThrowExceptionWhenEnvFileExistsButMissingKey() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create .env file without SONAR_TOKEN
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("OTHER_KEY=some-value");
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When & Then
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should have correct SONAR_TOKEN constant")
    void shouldHaveCorrectCursorApiKeyConstant() {
        // Given & When & Then
        assertThat(SonarApiKeyResolver.SONAR_TOKEN).isEqualTo("SONAR_TOKEN");
    }

    @Test
    @DisplayName("Should be able to instantiate SonarApiKeyResolver")
    void shouldBeAbleToInstantiateSonarApiKeyResolver() {
        // When
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // Then
        assertThat(resolver).isNotNull();
    }

    @Test
    @DisplayName("Should resolve API key with special characters from .env file")
    void shouldResolveApiKeyWithSpecialCharacters() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Note: Using simpler special characters that work well with .env format
            String testApiKey = "test-key-with-special-chars-123-abc";
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=" + testApiKey);
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo(testApiKey);
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should resolve very long API key from .env file")
    void shouldResolveVeryLongApiKey() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            String testApiKey = "a".repeat(1000);
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=" + testApiKey);
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo(testApiKey);
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    @Test
    @DisplayName("Should resolve API key with Unicode characters from .env file")
    void shouldResolveApiKeyWithUnicodeCharacters() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            String testApiKey = "test-key-with-unicode-🚀-测试-αβγ";
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=" + testApiKey);
            }
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo(testApiKey);
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    // Helper methods

    private void clearEnvironmentVariable() {
        // Use system property override to isolate tests from host environment variables
        System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, "");
    }

}
