package no.jobbscraper.argument;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Argument {

    HELP("--help", "--h", List.of("Display help")),
    DEBUG("--debug", "-b", List.of("Debug application yes/no (default)")),
    IP("--ip", "--ip", List.of("Specify the IP address")),
    PORT("--port", "--p", List.of("Specify the port number")),
    PATH("--path", "--ph", List.of("Specify the path")),
    REQUEST_PARAM("--request-param", "--rp", List.of("Specify the request parameter")),
    DISABLE_REST_CLIENT("--disable-rest-client", "--drc", List.of("Disable the REST client yes/no (default)")),
    START_PAGE("--start-page", "--sp", List.of("All scrapers will start at the given page")),
    DISABLED_SCRAPERS("--disabled-scrapers", "--ds",
            List.of("Enter a comma separated list of scrapers you wish to disable.",
                    "Example -> finn,karrierestart"));

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
