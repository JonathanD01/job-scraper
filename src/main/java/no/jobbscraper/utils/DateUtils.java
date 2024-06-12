package no.jobbscraper.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
        String[] datePatterns = {"dd. MMMM yyyy", "dd.MM.yyyy"};

        for (String pattern : datePatterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return LocalDate.parse(Objects.requireNonNull(StringUtils.removeWhitespace(deadline)), formatter);
            } catch (DateTimeParseException e) {
                // Ignore? What else?
            }
        }

        return null;
    }

}
