package info.jab.sonar.cli.model;

/**
 * Enum representing SonarCloud issue severities.
 */
public enum Severity {
    BLOCKER,
    CRITICAL,
    MAJOR,
    MINOR,
    INFO;

    /**
     * Factory method to create Severity from a string value.
     * Case-insensitive: allows values like "blocker", "BLOCKER", "Blocker", etc.
     *
     * @param value the string value to convert
     * @return the corresponding Severity enum value
     * @throws Exception if the value does not match any enum value
     */
    public static Severity from(String value) throws Exception {
        return Severity.valueOf(value.toUpperCase());
    }

    /**
     * Converts the severity to the format expected by the SonarCloud API.
     *
     * @return The API format string (the severity name)
     */
    public String toApiFormat() {
        return this.name();
    }
}

