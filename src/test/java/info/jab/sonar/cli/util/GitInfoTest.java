package info.jab.sonar.cli.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for GitInfo covering all branches.
 */
@DisplayName("GitInfo Tests")
class GitInfoTest {

    @Test
    @DisplayName("should print git info when properties are available")
    void print_validProperties_printsInfo() {
        // Given
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Properties props = new Properties();
        props.setProperty("git.build.version", "1.0.0");
        props.setProperty("git.commit.id.abbrev", "abc123");

        Supplier<InputStream> supplier = () -> {
            try {
                java.io.ByteArrayInputStream input = new java.io.ByteArrayInputStream(
                    ("git.build.version=1.0.0\ngit.commit.id.abbrev=abc123").getBytes()
                );
                return input;
            } catch (Exception e) {
                return null;
            }
        };

        GitInfo gitInfo = new GitInfo(supplier);

        try {
            // When
            gitInfo.print();

            // Then
            String output = outContent.toString();
            assertThat(output)
                .contains("Version:")
                .contains("1.0.0")
                .contains("Commit:")
                .contains("abc123");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("should print message when git.properties is not found")
    void print_nullInputStream_printsNotFoundMessage() {
        // Given
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Supplier<InputStream> supplier = () -> null;
        GitInfo gitInfo = new GitInfo(supplier);

        try {
            // When
            gitInfo.print();

            // Then
            String output = outContent.toString();
            assertThat(output).contains("git.properties not found");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("should handle IOException when reading properties")
    void print_ioException_printsErrorMessage() {
        // Given
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        Supplier<InputStream> supplier = () -> {
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("Test IO exception");
                }
            };
        };
        GitInfo gitInfo = new GitInfo(supplier);

        try {
            // When
            gitInfo.print();

            // Then
            String output = outContent.toString();
            assertThat(output).contains("Error printing git info");
        } finally {
            System.setOut(originalOut);
        }
    }


    @Test
    @DisplayName("should instantiate GitInfo with default constructor")
    void constructor_default_createsInstance() {
        // When
        GitInfo gitInfo = new GitInfo();

        // Then
        assertThat(gitInfo).isNotNull();
    }
}

