package no.jobbscraper.utils;

import java.util.Objects;

/**
 * Utility class for common string operations.
 */
public class StringUtils {

    /**
     * Removes all whitespace characters from the given string.
     *
     * @param str   The input string.
     * @return      The string with whitespace removed.
     */
    public static String removeWhitespace(String str) {
        if (Objects.isNull(str)) {
            return null;
        }
        return str.replaceAll(" ", "");
    }

    /**
     * Removes trailing comma from the given string if present.
     *
     * @param str   The input string.
     * @return      The string without the trailing comma.
     */
    public static String removeTrailingComma(String str) {
        if (Objects.isNull(str)) {
            return null;
        }
        if (str.endsWith(",")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * Checks if a string is not null, not empty, and not blank.
     *
     * @param   value The string to check.
     * @return  True if the string is not null, not empty, and not blank, false otherwise.
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.isEmpty() && !value.isBlank();
    }

    /**
     * Checks if the provided string is empty.
     *
     * @param value The string to check.
     * @return true if the string is not empty, false otherwise.
     */
    public static boolean isEmpty(String value) {
        return value == null || value.isEmpty() || value.isBlank();
    }

    /**
     * Removes the starting slash from the given string, if present.
     *
     * @param str   The input string.
     * @return      The modified string with the starting slash removed, or null if the input string is null.
     */
    public static String removeStartingSlash(String str) {
        if (Objects.isNull(str)) {
            return null;
        }
        if (str.startsWith("/")) {
            str = str.substring(1);
        }
        return str;
    }

    /**
     * Removes the trailing slash from the given string, if present.
     *
     * @param str   The input string.
     * @return      The modified string with the trailing slash removed, or null if the input string is null.
     */
    public static String removeTrailingSlash(String str) {
        if (Objects.isNull(str)) {
            return null;
        }
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }
        return str;
    }

    /**
     * Checks if a string consists of digits only.
     * Only works for positive numbers.
     * @param input is the string we are checking
     * @return true if positive number else false
     */
    public static boolean isPositiveNumber(String input){
        if (input == null || input.isEmpty()){
            return false;
        }

        for (int i = 0; i < input.length(); i++){
            if (!Character.isDigit(input.charAt(i))){
                return false;
            }
        }
        return true;
    }

}
