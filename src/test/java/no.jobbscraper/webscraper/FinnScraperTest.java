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

public class FinnScraperTest extends HtmlDocumentProvider {

    private static BaseWebScraper scraper;

    public FinnScraperTest() {
        super(scraper.getUrl(),
            "finn/finn_list_page.html",
            "finn/finn_detail_page.html");
    }

    @BeforeAll
    public static void setUp() {
        scraper = new FinnScraper();
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

        Assertions.assertTrue(actual.startsWith("https://www.finn.no/"));
    }

    @Test
    @DisplayName("Ensure image url is present")
    void itShouldExtractImageUrlForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Document document = getListViewDocument();
        Element element = scraper.extractJobPostElements(document).first();

        // Then
        String actual = scraper.extractImageUrlForJobPostFromElement(url, element);

        Assertions.assertTrue(actual.startsWith("https://images.finncdn.no"));
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

        Assertions.assertEquals("Join us to power a sustainable future with offshore wind!", actual);
    }

    @Test
    @DisplayName("Ensure extracting company name works")
    void itShouldExtractCompanyNameForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        String actual = scraper.extractCompanyNameForJobPostFromDoc(document);

        Assertions.assertEquals("NES Advantage Solutions AS", actual);
    }

    @Test
    @DisplayName("Ensure extracting company image url is empty")
    void itShouldExtractCompanyImageUrlForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        String actual = scraper.extractCompanyImageUrlForJobPostFromDoc(document);

        Assertions.assertEquals(null, actual);
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

        Assertions.assertEquals(null, actual);
    }

    @Test
    @DisplayName("Ensure extracting tags is empty")
    void itShouldExtractTagsForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        Set<String> expected = scraper.extractTagsForJobPostFromDoc(document);

        Assertions.assertFalse(expected.isEmpty());
        Assertions.assertEquals(5, expected.size());
    }

    @Test
    @DisplayName("Ensure extraction description map works")
    void itShouldExtractDescriptionMapForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        Map<String, Set<String>> result = scraper.extractDefinitionsMapForJobPostFromDoc(document);

        Assertions.assertTrue(result.containsKey("Stillingsfunksjon"));
        Assertions.assertTrue(result.containsKey("Sted"));
        Assertions.assertTrue(result.containsKey("Sektor"));
        Assertions.assertTrue(result.containsKey("Bransje"));
        Assertions.assertTrue(result.containsKey("Stillingstittel"));
    }

}
