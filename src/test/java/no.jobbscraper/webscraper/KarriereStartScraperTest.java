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

public class KarriereStartScraperTest extends HtmlDocumentProvider {

    private static BaseWebScraper scraper;

    public KarriereStartScraperTest() {
        super(scraper.getUrl(),
            "karrierestart/karrierestart_list_page.html",
            "karrierestart/karrierestart_detail_page.html");
    }

    @BeforeAll
    public static void setUp() {
        scraper = new KarriereStartScraper(10);
    }

    @Test
    @DisplayName("Ensure scanning is stopped when max page is reached")
    void itShouldNotContinueScan() {
        // Given
        int currentPage = 11;

        scraper.setPage(currentPage);
        // When
        boolean actual = scraper.continueScan();

        // Then
        Assertions.assertFalse(actual);
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

        Assertions.assertTrue(actual.startsWith("https://karrierestart.no/ledig-stilling/"));
    }

    @Test
    @DisplayName("Ensure image url is empty")
    void itShouldExtractImageUrlForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Document document = getListViewDocument();
        Element element = scraper.extractJobPostElements(document).first();

        // Then
        String actual = scraper.extractImageUrlForJobPostFromElement(url, element);

        Assertions.assertEquals(null, actual);
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

        Assertions.assertEquals("Vil du bli en del av Gjensidiges Graduateprogram?", actual);
    }

    @Test
    @DisplayName("Ensure extracting company name works")
    void itShouldExtractCompanyNameForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        String actual = scraper.extractCompanyNameForJobPostFromDoc(document);

        Assertions.assertEquals("Gjensidige", actual);
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

        Assertions.assertEquals("2024-10-02", actual.toString());
    }

    @Test
    @DisplayName("Ensure extracting tags is not empty")
    void itShouldExtractTagsForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        Set<String> actual = scraper.extractTagsForJobPostFromDoc(document);

        Assertions.assertEquals(
            Set.of("Oslo", "Graduateprogram?", "Vil", "Gjensidige", "en", "del", "Gjensidiges", "Heltid"),
            actual);
    }

    @Test
    @DisplayName("Ensure extracting job definitions works")
    void itShouldExtractDefinitionsMapForJobPostFromDoc() {
        // Given
        Document document = getDetailViewDocument();

        // Then
        Map<String, Set<String>> result = scraper.extractDefinitionsMapForJobPostFromDoc(document);

        Assertions.assertTrue(result.containsKey("Sted"));
        Assertions.assertTrue(result.containsKey("Ansettelsesform"));
        Assertions.assertTrue(result.containsKey("Stilling"));
        Assertions.assertTrue(result.containsKey("Bransje"));
    }

    @Test
    @DisplayName("Ensure max page is set")
    void itShouldSetMaxPage() {
        Assertions.assertNotEquals(0, scraper.getMaxPage());
    }

}
