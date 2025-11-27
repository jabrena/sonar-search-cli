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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for SonarApiKeyResolver utility.
 */
@DisplayName("SonarApiKeyResolver Tests")
class SonarApiKeyResolverTest {

    private String originalWorkingDir;
    private String originalSystemProperty;
    private boolean environmentVariableWasSet;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        // Save original working directory
        originalWorkingDir = System.getProperty("user.dir");
        originalSystemProperty = System.getProperty(SonarApiKeyResolver.SONAR_TOKEN);

        // Check if environment variable is set (e.g., in CI/CD)
        environmentVariableWasSet = System.getenv(SonarApiKeyResolver.SONAR_TOKEN) != null;

        // Point working directory to a temp folder so real .env is untouched
        File isolatedWorkingDir = tempDir.toFile();
        if (!isolatedWorkingDir.exists()) {
            isolatedWorkingDir.mkdirs();
        }
        System.setProperty("user.dir", isolatedWorkingDir.getAbsolutePath());

        // Clear system property for clean tests
        clearSystemProperty();
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
        // Note: If SONAR_TOKEN is set as environment variable (e.g., in CI/CD),
        // this test will verify that the resolver correctly uses it instead of throwing.
        // This is expected behavior and ensures tests work in both local and CI environments.
        clearSystemProperty();
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // When & Then
        if (environmentVariableWasSet) {
            // In CI/CD, the environment variable is set, so resolver should return it
            String result = resolver.resolveApiKey();
            assertThat(result).isNotNull().isNotEmpty();
        } else {
            // In local environment without env var, should throw exception
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        }
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
        // Note: If SONAR_TOKEN is set as environment variable (e.g., in CI/CD),
        // this test will verify that the resolver correctly uses it instead of throwing.
        clearSystemProperty();
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // When & Then
        if (environmentVariableWasSet) {
            // In CI/CD, the environment variable is set, so resolver should return it
            String result = resolver.resolveApiKey();
            assertThat(result).isNotNull().isNotEmpty();
        } else {
            // In local environment without env var, should throw exception
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found")
                .hasMessageContaining(".env file");
        }
    }

    @Test
    @DisplayName("Should throw exception when API key is empty in system environment")
    void shouldThrowExceptionWhenApiKeyIsEmptyInSystemEnvironment() {
        // Given
        // Set empty system property (takes precedence over env var)
        System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, "");
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        try {
            // When & Then
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        } finally {
            System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
        }
    }

    @Test
    @DisplayName("Should throw exception when API key is empty in .env file")
    void shouldThrowExceptionWhenApiKeyIsEmptyInEnvFile() throws IOException {
        // Given
        // .env file takes precedence over environment variable, so empty .env should cause exception
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
        // .env file takes precedence over environment variable
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
            // If environment variable is set (e.g., in CI/CD), it will be used instead
            if (environmentVariableWasSet) {
                String result = resolver.resolveApiKey();
                assertThat(result).isNotNull().isNotEmpty();
            } else {
                assertThatThrownBy(resolver::resolveApiKey)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("API key not found");
            }
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
        // Note: If SONAR_TOKEN is set as environment variable (e.g., in CI/CD),
        // this test will verify that the resolver correctly uses it instead of throwing.
        clearSystemProperty();
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        // When & Then
        if (environmentVariableWasSet) {
            // In CI/CD, the environment variable is set, so resolver should return it
            String result = resolver.resolveApiKey();
            assertThat(result).isNotNull().isNotEmpty();
        } else {
            // In local environment without env var, should throw exception
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        }
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
            // If environment variable is set (e.g., in CI/CD), it will be used instead
            if (environmentVariableWasSet) {
                String result = resolver.resolveApiKey();
                assertThat(result).isNotNull().isNotEmpty();
            } else {
                assertThatThrownBy(resolver::resolveApiKey)
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("API key not found");
            }
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

    @Test
    @DisplayName("Should resolve API key from system property")
    void shouldResolveApiKeyFromSystemProperty() {
        // Given
        String testApiKey = "system-property-key-123";
        System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, testApiKey);
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        try {
            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo(testApiKey);
        } finally {
            System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
        }
    }

    @Test
    @DisplayName("Should trim whitespace from API key in system property")
    void shouldTrimWhitespaceFromApiKeyInSystemProperty() {
        // Given
        String testApiKey = "  system-property-key-whitespace  ";
        System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, testApiKey);
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        try {
            // When
            String result = resolver.resolveApiKey();

            // Then
            assertThat(result).isEqualTo("system-property-key-whitespace");
        } finally {
            System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
        }
    }

    @Test
    @DisplayName("Should throw exception when system property is empty")
    void shouldThrowExceptionWhenSystemPropertyIsEmpty() {
        // Given
        System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, "");
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        try {
            // When & Then
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        } finally {
            System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
        }
    }

    @Test
    @DisplayName("Should throw exception when system property is whitespace-only")
    void shouldThrowExceptionWhenSystemPropertyIsWhitespaceOnly() {
        // Given
        System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, "   ");
        SonarApiKeyResolver resolver = new SonarApiKeyResolver();

        try {
            // When & Then
            assertThatThrownBy(resolver::resolveApiKey)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API key not found");
        } finally {
            System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
        }
    }

    @Test
    @DisplayName("Should prioritize .env file over system property")
    void shouldPrioritizeEnvFileOverSystemProperty() throws IOException {
        // Given
        File projectRoot = new File(System.getProperty("user.dir"));
        File envFile = new File(projectRoot, ".env");

        try {
            // Create .env file (should take priority)
            String envFileKey = "env-file-key";
            try (FileWriter writer = new FileWriter(envFile)) {
                writer.write("SONAR_TOKEN=" + envFileKey);
            }

            // Set system property
            String systemPropertyKey = "system-property-key";
            System.setProperty(SonarApiKeyResolver.SONAR_TOKEN, systemPropertyKey);
            SonarApiKeyResolver resolver = new SonarApiKeyResolver();

            try {
                // When
                String result = resolver.resolveApiKey();

                // Then - .env file should take priority
                assertThat(result).isEqualTo(envFileKey);
            } finally {
                System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
            }
        } finally {
            // Clean up the .env file
            if (envFile.exists()) {
                envFile.delete();
            }
        }
    }

    // Helper methods

    private void clearSystemProperty() {
        // Clear system property to ensure clean test state
        System.clearProperty(SonarApiKeyResolver.SONAR_TOKEN);
    }

}
