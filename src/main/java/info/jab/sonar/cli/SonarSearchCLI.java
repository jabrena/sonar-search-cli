package info.jab.sonar.cli;

import info.jab.sonar.cli.client.SonarHttpClient;
import info.jab.sonar.cli.model.IssueType;
import info.jab.sonar.cli.util.GitInfo;
import info.jab.sonar.cli.util.SonarApiKeyResolver;

import com.diogonunes.jcolor.Attribute;
import static com.diogonunes.jcolor.Ansi.colorize;
import com.github.lalyos.jfiglet.FigletFont;

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

    @CommandLine.Option(
        names = {"--type", "-t"},
        description = "Issue type: ${COMPLETION-CANDIDATES}",
        required = true
    )
    private IssueType type;

    @CommandLine.Option(
        names = {"--component", "-c"},
        description = "Component key (e.g., jabrena_sonar-search-cli)",
        required = true
    )
    private String componentKey;

    @CommandLine.Option(
        names = {"--quiet", "-q"},
        description = "Suppress banner output",
        defaultValue = "false"
    )
    private boolean quiet;

    // Dependencies
    private final SonarApiKeyResolver apiKeyResolver;
    private final SonarHttpClient sonarHttpClient;

    /**
     * Default constructor that initializes all dependencies.
     */
    public SonarSearchCLI() {
        this.apiKeyResolver = new SonarApiKeyResolver();
        this.sonarHttpClient = new SonarHttpClient();
    }

    /**
     * Constructor for testing that accepts all dependencies.
     */
    SonarSearchCLI(SonarApiKeyResolver apiKeyResolver, SonarHttpClient sonarHttpClient) {
        this.apiKeyResolver = apiKeyResolver;
        this.sonarHttpClient = sonarHttpClient;
    }

    @Override
    public void run() {
        try {
            String apiKey = apiKeyResolver.resolveApiKey();
            String response = sonarHttpClient.searchIssues(apiKey, componentKey, type);
            System.out.println(response);
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
