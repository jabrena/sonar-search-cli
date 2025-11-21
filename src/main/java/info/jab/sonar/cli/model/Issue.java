package info.jab.sonar.cli.model;

/**
 * Enum representing SonarCloud issue types.
 */
public enum Issue {
    BUG,
    CODE_SMELL,
    VULNERABILITY,
    ALL;

    /**
     * Factory method to create Issue from a string value.
     * Case-insensitive: allows values like "bug", "BUG", "Bug", etc.
     *
     * @param value the string value to convert
     * @return the corresponding Issue enum value
     * @throws Exception if the value does not match any enum value
     */
    public static Issue from(String value) throws Exception {
        return Issue.valueOf(value.toUpperCase());
    }

    /**
     * Converts the issue type to the format expected by the SonarCloud API.
     *
     * @return The API format string (comma-separated for ALL, single type otherwise)
     */
    public String toApiFormat() {
        if (this == ALL) {
            return "BUG,CODE_SMELL,VULNERABILITY";
        }
        return this.name();
    }
}

