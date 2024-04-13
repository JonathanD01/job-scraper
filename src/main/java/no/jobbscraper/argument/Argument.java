package no.jobbscraper.argument;

import no.jobbscraper.Main;
import no.jobbscraper.url.WebsiteURL;
import no.jobbscraper.utils.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public enum Argument {

    HELP("--help", "--h", List.of("Display help")),
    IP("--ip", "--ip", List.of("Specify the IP address")),
    PORT("--port", "--p", List.of("Specify the port number")),
    PATH("--path", "--ph", List.of("Specify the path")),
    REQUEST_PARAM("--request-param", "--rp", List.of("Specify the request parameter")),
    DISABLE_REST_CLIENT("--disable-rest-client", "--drc", List.of("Disable the REST client")),
    START_PAGE("--start-page", "--sp", List.of("All scrapers will start at the given page")),
    DISABLED_SCRAPERS("--disabled-scrapers", "--ds",
            List.of("Enter a comma separated list of the urls you wish to disable.",
                    "Example -> https://www.finn.no/job/fulltime/search.html,https://karrierestart.no/jobb"));

    private final static Map<Argument, String> MAP = new HashMap<>();
    private final String name;
    private final String shortName;
    private final List<String> help;

    Argument(String name, String shortName, List<String> help) {
        this.name = name;
        this.shortName = shortName;
        this.help = help;
    }

    public static Argument from(String argumentName) {
        return Arrays.stream(Argument.values())
                .filter(argumentEnum -> argumentEnum.get().equalsIgnoreCase(argumentName)
                        || argumentEnum.getShortName().equalsIgnoreCase(argumentName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Could not parse " + argumentName +
                        " into a valid Argument"));
    }

    public static Map<Argument, String> getMap() {
        return new HashMap<>(MAP);
    }

    public static void put(Argument argument, String value) {
        MAP.put(argument, value);
    }

    public static String getValue(Argument argument) {
        return MAP.getOrDefault(argument, null);
    }

    public final String get() {
        return this.name;
    }

    public final String getShortName() {
        return this.shortName;
    }

    public List<String> getHelp() {
        return this.help;
    }

}
