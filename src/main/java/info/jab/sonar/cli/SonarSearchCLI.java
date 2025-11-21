package info.jab.sonar.cli;

import info.jab.sonar.cli.model.Issue;
import info.jab.sonar.cli.model.Query;
import info.jab.sonar.cli.model.Severity;
import info.jab.sonar.cli.model.Status;
import info.jab.sonar.cli.service.SonarService;
import info.jab.sonar.cli.util.GitInfo;
import info.jab.sonar.cli.util.SonarApiKeyResolver;

import com.diogonunes.jcolor.Attribute;
import static com.diogonunes.jcolor.Ansi.colorize;
import com.github.lalyos.jfiglet.FigletFont;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.function.Supplier;
import picocli.CommandLine;

/**
 * Main CLI application for Sonar Search.
 * Command to search SonarCloud issues by type.
 */
@CommandLine.Command(
    name = "sonar-search",
    mixinStandardHelpOptions = true,
    usageHelpAutoWidth = true,
    description = "Search SonarCloud issues by type"
)
public class SonarSearchCLI implements Runnable {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @CommandLine.Option(
        names = { "--project", "-p" },
        description = "Project key (e.g., jabrena_sonar-search-cli)",
        required = false
    )
    private String projectKey;


    @CommandLine.Option(
        names = {"--quiet", "-q"},
        description = "Suppress banner output",
        defaultValue = "false"
    )
    private boolean quiet;

    @CommandLine.Option(
        names = { "--query" },
        description = "Query type: ${COMPLETION-CANDIDATES}",
        required = false,
        converter = QueryConverter.class
    )
    private Query query;

    /**
     * Simple converter that delegates to Query.from().
     */
    private static class QueryConverter implements CommandLine.ITypeConverter<Query> {
        @Override
        public Query convert(String value) throws Exception {
            return Query.from(value);
        }
    }

    /**
     * Simple converter that delegates to Issue.from().
     */
    private static class IssueConverter implements CommandLine.ITypeConverter<Issue> {
        @Override
        public Issue convert(String value) throws Exception {
            return Issue.from(value);
        }
    }

    /**
     * Simple converter that delegates to Severity.from().
     */
    private static class SeverityConverter implements CommandLine.ITypeConverter<Severity> {
        @Override
        public Severity convert(String value) throws Exception {
            return Severity.from(value);
        }
    }

    /**
     * Simple converter that delegates to Status.from().
     */
    private static class StatusConverter implements CommandLine.ITypeConverter<Status> {
        @Override
        public Status convert(String value) throws Exception {
            return Status.from(value);
        }
    }

    @CommandLine.Option(
        names = { "--types", "-t" },
        description = "Issue type: ${COMPLETION-CANDIDATES}",
        required = false,
        converter = IssueConverter.class
    )
    private Issue types;

    @CommandLine.Option(
        names = { "--severity", "-s" },
        description = "Issue severity: ${COMPLETION-CANDIDATES}",
        required = false,
        converter = SeverityConverter.class
    )
    private Severity severity;

    @CommandLine.Option(
        names = { "--status" },
        description = "Issue status: ${COMPLETION-CANDIDATES}",
        required = false,
        converter = StatusConverter.class
    )
    private Status status;

    @CommandLine.Option(
        names = { "--size" },
        description = "Page size (number of results per page). Valid range: 1-500. Default: 100",
        required = false,
        defaultValue = "100"
    )
    private Integer size;

    @CommandLine.Option(
        names = { "--detail" },
        description = "Issue key-id to get detailed information about a specific issue",
        required = false
    )
    private String detail;

    // Dependencies
    private final SonarApiKeyResolver apiKeyResolver;
    private final SonarService sonarService;

    /**
     * Default constructor that initializes all dependencies.
     */
    public SonarSearchCLI() {
        this.apiKeyResolver = new SonarApiKeyResolver();
        this.sonarService = new SonarService();
    }

    /**
     * Constructor for testing that accepts all dependencies.
     */
    SonarSearchCLI(SonarApiKeyResolver apiKeyResolver, SonarService sonarService) {
        this.apiKeyResolver = apiKeyResolver;
        this.sonarService = sonarService;
    }

    @Override
    public void run() {
        try {
            // Validate that --query is provided
            if (query == null) {
                throw new RuntimeException("Must provide --query");
            }

            // Validate --detail usage
            if (detail != null) {
                // When using --detail, --query is required and --types, --severity, --status are not allowed
                if (types != null || severity != null || status != null) {
                    throw new RuntimeException("--detail cannot be used with --types, --severity, or --status");
                }
            } else {
                // When not using --detail, --project is required
                if (projectKey == null) {
                    throw new RuntimeException("Must provide --project when not using --detail");
                }
                // Validate that --types is provided when --query is ISSUES
                if (query == Query.ISSUES && types == null) {
                    throw new RuntimeException("Must provide --types when --query is ISSUES");
                }
            }

            // Validate --size is in valid range (1-500)
            if (size < 1 || size > 500) {
                throw new RuntimeException("--size must be between 1 and 500");
            }

            String apiKey = apiKeyResolver.resolveApiKey();
            boolean tokenValid = sonarService.validateToken(apiKey);
            if (!tokenValid) {
                throw new RuntimeException("SONAR_TOKEN validation failed");
            }
            if (!quiet) {
                System.out.println("✓ SONAR_TOKEN validated");
                System.out.println();
                System.out.println();
            }

            String response;
            if (detail != null) {
                // Use --detail with --query to get issue or hotspot details by key-id
                if (query == Query.ISSUES) {
                    response = sonarService.searchIssueDetail(detail, apiKey);
                } else if (query == Query.HOTSPOTS) {
                    response = sonarService.searchHotspotDetail(detail, apiKey);
                } else {
                    throw new RuntimeException("--detail is only supported with --query ISSUES or HOTSPOTS");
                }
            } else if (query == Query.HOTSPOTS) {
                response = sonarService.searchHotspots(projectKey, apiKey);
            } else {
                // For ISSUES, use the --types value and optional --severity, --status, and --size
                response = sonarService.searchIssues(projectKey, types, severity, status, size, apiKey);
            }

            // Pretty-print JSON
            try {
                JsonNode jsonNode = OBJECT_MAPPER.readTree(response);
                String prettyJson = OBJECT_MAPPER.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonNode);
                System.out.println(prettyJson);
            } catch (Exception e) {
                // If JSON parsing fails, output raw response
                System.out.println(response);
            }
        } catch (Exception e) {
            System.err.println("Error executing search: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Main entry point for the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SonarSearchCLI cli = new SonarSearchCLI();
        CommandLine cmd = new CommandLine(cli);

        // Parse arguments to populate fields (including quiet flag)
        // If parsing fails, execute() will handle the error properly
        try {
            cmd.parseArgs(args);
            // Print banner only if not quiet (checking the parsed field)
            if (!cli.quiet) {
                printBanner(() -> new GitInfo());
            }
        } catch (CommandLine.ParameterException e) {
            // If parsing fails, don't print banner and let execute() handle the error
        }

        int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }

    /**
     * Prints the application banner.
     * Package-private method for testing with injected GitInfo supplier.
     *
     * @param gitInfoSupplier Supplier for creating GitInfo instance
     */
    static void printBanner(Supplier<GitInfo> gitInfoSupplier) {
        try {
            System.out.println();
            String asciiArt = FigletFont.convertOneLine("Sonar Search CLI");
            System.out.println(colorize(asciiArt, Attribute.GREEN_TEXT()));
            gitInfoSupplier.get().print();
        } catch (IOException e) {
            System.out.println("Error printing banner: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Error printing banner: " + e.getMessage());
        }
    }

}
