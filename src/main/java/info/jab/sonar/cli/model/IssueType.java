package info.jab.sonar.cli.model;

/**
 * Enum representing SonarCloud issue types.
 */
public enum IssueType {
    BUG,
    CODE_SMELL,
    VULNERABILITY,
    ALL;

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

