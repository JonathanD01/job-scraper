package no.jobbscraper;

import no.jobbscraper.argument.Argument;
import no.jobbscraper.database.Database;
import no.jobbscraper.url.WebsiteURL;
import no.jobbscraper.utils.StringUtils;
import no.jobbscraper.webscraper.ArbeidsplassenNavScraper;
import no.jobbscraper.webscraper.BaseWebScraper;
import no.jobbscraper.webscraper.FinnScraper;
import no.jobbscraper.webscraper.KarriereStartScraper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        readLoggingConfiguration();

        if (!createLogFolder()) {
            return;
        }

        setUpArguments(args);

        Database.setUp();

        setUpWebScrapers();
    }

    /**
     * Reads the logging configuration from the "/logging.properties" file.
     * This method sets up the logging properties for the application.
     * If the file is not found or cannot be read, a RuntimeException is thrown.
     */
    private static void readLoggingConfiguration() {
        try (InputStream stream = Main.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a log folder at the default location, which is the user's home directory appended with "/jobbscraper".
     * If the folder does not exist, it attempts to create it.
     * If creation is successful, a message is printed indicating the folder's location.
     * If creation fails, an error message is printed.
     *
     * @return {@code true} if the folder was created or already exists, {@code false} otherwise.
     */
    private static boolean createLogFolder() {
        String folderPath = System.getProperty("user.home") + "/jobbscraper";
        File folder = new File(folderPath);

        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                System.out.println("Log folder created at: " + folder.getAbsolutePath());
            } else {
                System.out.println("Failed to create log folder at: " + folder.getAbsolutePath());
            }
            return created;
        } else {
            System.out.println("Log folder exists at: " + folder.getAbsolutePath());
            return true;
        }
    }

    /**
     * Parses the command-line arguments and sets up the application accordingly.
     * It processes the arguments in pairs, where the first argument is the argument name and the second is its value.
     * The method handles the "help" argument separately and exits the program if it's present.
     * It then populates the Argument map with the provided arguments.
     * Finally, it logs the arguments being used.
     *
     * @param args The command-line arguments.
     */
    private static void setUpArguments(String[] args) {
        checkForHelpArgument(args);

        try {
            for (int i = 0; i < args.length - 1; i+= 2) {
                String argumentValue = StringUtils.removeStartingSlash(args[i+1]);
                argumentValue = Objects.requireNonNull(StringUtils.removeTrailingSlash(argumentValue))
                        .toLowerCase();

                Argument argument = Argument.from(args[i]);

                validateArgument(argument, argumentValue);

                Argument.put(argument, argumentValue);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException  e) {
            logger.severe("Please provide all flags...");
            System.exit(0);
        }
        logger.info("Running program with arguments -> " + Argument.getMap());
    }

    /**
     * Helper function to validate certain arguments
     * As of now it will check for the IP argument
     * and make sure http is not included.
     *
     * If there is a validation error the program will exit.
     * @param argument is the argument to validate
     * @param argumentValue is the argument value to validate
     */
    private static void validateArgument(Argument argument, String argumentValue) {
        if (argument.equals(Argument.IP) && argumentValue.startsWith("http")) {
            System.out.println("Remove the http protocol from the ip");
            logger.severe("Remove the http protocol from the ip");
            System.exit(0);
        }
    }

    /**
     * Displays the help information for the application's command-line arguments.
     * It iterates through each argument and prints its name, short name, and help details.
     * It also lists all the URLs that can be disabled.
     * After displaying the help information, the program exits.
     */
    private static void showArgumentHelp() {
        // Display argument help
        System.out.println("\n[FinnJob scraper help]\n\n");
        Arrays.stream(Argument.values()).forEach(argument -> {
            // First row
            System.out.format("%-50s %-50s %n", "Argument name", "Short argument name");

            // Second row
            System.out.format("%-50s %-50s %n", argument.get(), argument.getShortName());

            // Third row for help
            System.out.print("\nHelp:\n");
            argument.getHelp().forEach(helpLine -> System.out.format("%-5s %n", helpLine));

            System.out.println();
        });

        // Display all URLs that can be disabled
        System.out.println("\nAll urls you can disable:\n");
        Arrays.stream(WebsiteURL.values())
                .forEach(websiteURL -> System.out.println(websiteURL.get()));

        System.exit(0);
    }

    /**
     * Checks if the command-line arguments contain the "help" argument.
     * If the "help" argument is found, it calls the {@code showArgumentHelp()} method.
     *
     * @param args The command-line arguments to check.
     */
    private static void checkForHelpArgument(String[] args) {
        boolean doesArgsContainHelp = Arrays.stream(args)
                .anyMatch(arg -> arg.equalsIgnoreCase(Argument.HELP.get())
                        || arg.equalsIgnoreCase(Argument.HELP.getShortName()));

        if (doesArgsContainHelp) {
            showArgumentHelp();
        }
    }

    /**
     * Sets up the web scrapers for various job posting websites.
     * It creates instances of the web scrapers and executes them using virtual threads.
     */
    private static void setUpWebScrapers() {
        Set<BaseWebScraper> webScrapers = Set.of(
                new ArbeidsplassenNavScraper(),
                new KarriereStartScraper(),
                new FinnScraper()
        );

        // Virtual threads? Wohooo
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            webScrapers.forEach(baseWebScraper -> executorService.execute(baseWebScraper::scan));
        }
    }
}