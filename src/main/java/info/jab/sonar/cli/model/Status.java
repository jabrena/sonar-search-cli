package info.jab.sonar.cli.model;

/**
 * Enum representing SonarCloud issue statuses.
 */
public enum Status {
    OPEN,
    CLOSED,
    CONFIRMED,
    REOPENED,
    RESOLVED;

    /**
     * Factory method to create Status from a string value.
     * Case-insensitive: allows values like "open", "OPEN", "Open", etc.
     *
     * @param value the string value to convert
     * @return the corresponding Status enum value
     * @throws Exception if the value does not match any enum value
     */
    public static Status from(String value) throws Exception {
        return Status.valueOf(value.toUpperCase());
    }

    /**
     * Converts the status to the format expected by the SonarCloud API.
     *
     * @return The API format string (the status name)
     */
    public String toApiFormat() {
        return this.name();
    }
}

