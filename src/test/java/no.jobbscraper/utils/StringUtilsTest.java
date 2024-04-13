package no.jobbscraper.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {

    @Test
    @DisplayName("Ensure removing whitespace works")
    void itShouldRemoveWhitespace() {
        // Given
        String str = "   s o m    e whitespace";
        String expected = "somewhitespace";

        // When
        String actual = StringUtils.removeWhitespace(str);

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure removing trailing comma works")
    void itShouldRemoveTrailingComma() {
        // Given
        String str = "sjokolade,";
        String expected = "sjokolade";

        // When
        String actual = StringUtils.removeTrailingComma(str);

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure removing trailing char does not work")
    void itShouldNotRemoveTrailingComma() {
        // Given
        String str = "sjokolade,a";
        String expected = "sjokolade,a";

        // When
        String actual = StringUtils.removeTrailingComma(str);

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure non-empty strings returns true")
    void itShouldNotReturnNotEmpty() {
        // Given
        String nonEmptyString = "wowowowow";

        // When
        boolean actual = StringUtils.isNotEmpty(nonEmptyString);

        // Then
        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("Ensure that an empty string is correctly identified as empty")
    void itShouldReturnEmpty() {
        // Given
        String emptyString = "";

        // When
        boolean actual = StringUtils.isNotEmpty(emptyString);

        // Then
        Assertions.assertFalse(actual);
    }

    @Test
    @DisplayName("Ensure null strings return true as empty")
    void itShouldBeEmptyForNull() {
        // Given
        // When
        boolean actual = StringUtils.isEmpty(null);
        // Then
        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("Ensure empty strings return true as empty")
    void itShouldBeEmptyForEmpty() {
        // Given
        String emptyString = "";
        // When
        boolean actual = StringUtils.isEmpty(emptyString);
        // Then
        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("Ensure blank strings return true as empty")
    void itShouldBeEmptyForBlank() {
        // Given
        String blankString = "   ";
        // When
        boolean actual = StringUtils.isEmpty(blankString);
        // Then
        Assertions.assertTrue(actual);
    }

    @Test
    @DisplayName("Ensure non-empty string return false")
    void itShouldNotBeEmptyForString() {
        // Given
        String blankString = "Oslo";
        // When
        boolean actual = StringUtils.isEmpty(blankString);
        // Then
        Assertions.assertFalse(actual);
    }

    @Test
    @DisplayName("Ensure starting slash is removed")
    void itShouldRemoveStartingSlash() {
        // Given
        String stringWithStartingSlash = "/i/am/starting/with/slash";
        String expected = "i/am/starting/with/slash";

        // When
        String actual = StringUtils.removeStartingSlash(stringWithStartingSlash);

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure starting char is not removed")
    void itShouldNotRemoveStartingSlash() {
        // Given
        String stringWithStartingSlash = "ai/am/starting/with/slash";
        String expected = "ai/am/starting/with/slash";

        // When
        String actual = StringUtils.removeStartingSlash(stringWithStartingSlash);

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure trailing slash is removed")
    void itShouldRemoveTrailingSlash() {
        // Given
        String stringWithTrailingSlash = "/i/am/a/trailing/slash/";
        String expected = "/i/am/a/trailing/slash";

        // When
        String actual = StringUtils.removeTrailingSlash(stringWithTrailingSlash);

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure trailing char is not removed")
    void itShouldNotRemoveTrailingSlash() {
        // Given
        String stringWithTrailingSlash = "/i/am/a/trailing/slash/a";
        String expected = "/i/am/a/trailing/slash/a";

        // When
        String actual = StringUtils.removeTrailingSlash(stringWithTrailingSlash);

        // Then
        Assertions.assertEquals(expected, actual);
    }


}
