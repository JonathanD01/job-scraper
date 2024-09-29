package no.jobbscraper.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Objects;

public class DateUtils {

    /**
     * Parses a deadline string into a Date object.
     *
     * @param deadline  The deadline string to parse.
     * @return A        Date object representing the parsed deadline, or null if the deadline string cannot be parsed.
     */
    public static LocalDate parseDeadline(String deadline) {
        if (StringUtils.isEmpty(deadline)) {
            return null;
        }

        // TODO IMPROVE
        deadline = deadline.contains("Søk") ? extractDateStartingFromFirstDigit(deadline) : deadline;

        // Patterns to match different date formats
        String[] datePatterns = {"dd.MMMMyyyy", "dd.MM.yyyy", "dd.MMMM.yyyy"};

        for (String pattern : datePatterns) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("nb"));

            try {
                return LocalDate.parse(Objects.requireNonNull(StringUtils.removeWhitespace(deadline)), formatter);
            } catch (DateTimeParseException e) {
                // Ignore this pattern and try the next one
            }
        }
        return null;
    }

    /**
     * Extracts a substring starting from the first digit in the given input string.
     * If the string does not contain any digits, returns the entire input string.
     * Additionally, appends the current year to the result if it is not already present.
     *
     * @param input the original input string to extract from
     * @return a substring starting from the first digit, appended with the current year if missing
     */
    private static String extractDateStartingFromFirstDigit(String input) {
        String result = null;

        // Loop through the string to find the first digit
        for (int i = 0; i < input.length(); i++) {
            if (Character.isDigit(input.charAt(i))) {
                result = input.substring(i);
                break;
            }
        }

        // If no digit is found, return the entire input
        if (result == null) {
            result = input;
        }

        // Append the current year if not already present
        int currentYear = LocalDate.now().getYear();
        if (!result.endsWith(String.valueOf(currentYear))) {
            result += " " + currentYear;
        }

        return result;
    }

}
