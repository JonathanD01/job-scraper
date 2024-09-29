package no.jobbscraper.webscraper;

import no.jobbscraper.HtmlDocumentProvider;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public class ArbeidsplassenNavScraperTest extends HtmlDocumentProvider {

    private static BaseWebScraper scraper;

    public ArbeidsplassenNavScraperTest() {
        super(scraper.getUrl(),
            "nav/nav_list_page.html",
            "nav/nav_detail_page.html");
    }

    @BeforeAll
    public static void setUp() {
        scraper = new ArbeidsplassenNavScraper();
    }

    @Test
    @DisplayName("Ensure getting first page works")
    void itShouldGetFirstPage() {
        // Given
        int page = 1;
        int expected = page * 25;

        scraper.setPage(page);

        // When
        int actual = scraper.getPage();

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure getting second page works")
    void itShouldGetSecondPage() {
        // Given
        int page = 2;
        int expected = page * 25;

        scraper.setPage(page);

        // When
        int actual = scraper.getPage();

        // Then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting url works")
    public void itShouldExtractUrlForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Document document = getListViewDocument();
        Element element = scraper.extractJobPostElements(document).first();

        // Then
        String actual = scraper.extractUrlForJobPostFromElement(url, element);

        Assertions.assertTrue(actual.startsWith("https://arbeidsplassen.nav.no"));
    }

    @Test
    @DisplayName("Ensure extracting title works")
    void itShouldExtractTitleForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Document document = getListViewDocument();
        Element element = scraper.extractJobPostElements(document).first();

        // Then
        String actual = scraper.extractTitleForJobPostFromElement(url, element);

        Assertions.assertEquals("Tannlege", actual);
    }

    @Test
    @DisplayName("Ensure extracting company name works")
    void itShouldExtractCompanyNameForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        String actual = scraper.extractCompanyNameForJobPostFromDoc(document);

        Assertions.assertEquals("Triaden Tannklinikk As", actual);
    }

    @Test
    @DisplayName("Ensure extracting description works")
    void itShouldExtractDescriptionForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        String actual = scraper.extractDescriptionForJobPostFromDoc(document);

        Assertions.assertNotNull(actual);
    }

    @Test
    @DisplayName("Ensure extract deadline works")
    void itShouldExtractDeadlineForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        LocalDate actual = scraper.extractDeadlineForJobPostFromDoc(document);

        Assertions.assertEquals("2024-10-20", actual.toString());
    }

    @Test
    @DisplayName("Ensure extraction description map works")
    void itShouldExtractDescriptionMapForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        Map<String, Set<String>> result = scraper.extractDefinitionsMapForJobPostFromDoc(document);

        Assertions.assertTrue(result.containsKey("Arbeidstid"));
        Assertions.assertTrue(result.containsKey("Oppstart"));
        Assertions.assertTrue(result.containsKey("Arbeidstid"));
        Assertions.assertTrue(result.containsKey("Stillinger"));
        Assertions.assertTrue(result.containsKey("Arbeidsspråk"));
        Assertions.assertTrue(result.containsKey("Stillingstittel"));
    }

}
