package info.jab.sonar.cli.model;

/**
 * Enum representing query types for SonarCloud search.
 */
public enum Query {
    ISSUES,
    HOTSPOTS,
    DUPLICATIONS;

    /**
     * Factory method to create Query from a string value.
     * Case-insensitive: allows values like "issues", "ISSUES", "Issues", etc.
     *
     * @param value the string value to convert
     * @return the corresponding Query enum value
     * @throws Exception if the value does not match any enum value
     */
    public static Query from(String value) throws Exception {
        return Query.valueOf(value.toUpperCase());
    }
}

