package no.jobbscraper.webscraper;

import no.jobbscraper.utils.DateUtils;
import no.jobbscraper.utils.ElementSearchQuery;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KarriereStartScraperTest {

    private static BaseWebScraper scraper;

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
        Element mockElement = mock(Element.class);

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, mockElement)
                .setCssQuery("a.j-title")
                .attributeToReturn("abs:href")
                .build();

        String expected = url + "/job/123";

        // When
        when(mockElement.expectFirst(searchQuery.cssQuery())).thenReturn(mockElement);
        when(mockElement.attr(searchQuery.attributeToReturn())).thenReturn(expected);

        // Then
        String actual = scraper.extractUrlForJobPostFromElement(url, mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure image url is empty")
    void itShouldExtractImageUrlForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Element mockElement = mock(Element.class);

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, mockElement)
                .setCssQuery("div.j-presentation.j-presentation-overflowed > a > img[src]")
                .attributeToReturn("abs:src")
                .build();

        String expected = url + "/image/path/karriere";

        // When
        when(mockElement.attr(searchQuery.attributeToReturn())).thenReturn(expected);
        when(mockElement.expectFirst(searchQuery.cssQuery())).thenReturn(mockElement);

        // Then
        String actual = scraper.extractImageUrlForJobPostFromElement(url, mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting title works")
    void itShouldExtractTitleForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Element mockElement = mock(Element.class);

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, mockElement)
                .setCssQuery("a.j-title > span")
                .text()
                .build();


        String expected = "Senior utvikler hos KarriereStart!";

        // When
        when(mockElement.expectFirst(searchQuery.cssQuery())).thenReturn(mockElement);
        when(mockElement.text()).thenReturn(expected);

        // Then
        String actual = scraper.extractTitleForJobPostFromElement(url, mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting company name works")
    void itShouldExtractCompanyNameForJobPostFromDoc() {
        // Given
        String expected = "KarriereStart";

        Document mockDocument = mock(Document.class);
        Element mockFirstElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockFirstElement));

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(mockDocument)
                .setXpath("//div[@class='menu-item topic-header-text']")
                .ownText()
                .build();

        // When
        when(mockDocument.selectXpath(searchQuery.XPath())).thenReturn(mockElements);
        when(mockFirstElement.ownText()).thenReturn(expected);

        // Then
        String actual = scraper.extractCompanyNameForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting company image url is empty")
    void itShouldExtractCompanyImageUrlForJobPostFromDoc() {
        // Given
        String url = scraper.getCurrentUrl();
        Document mockDocument = mock(Document.class);
        Element mockElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement));

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(mockDocument)
                .setXpath("//div[@class='cp_header_logo']/a/img")
                .attributeToReturn("abs:src")
                .build();

        String expected = url + "/dynamic/1600w/logo/logo";

        // When
        when(mockDocument.selectXpath(searchQuery.XPath())).thenReturn(mockElements);
        when(mockElement.attr(searchQuery.attributeToReturn())).thenReturn(expected);

        // Then
        String actual = scraper.extractCompanyImageUrlForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting description works")
    void itShouldExtractDescriptionForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);
        Element mockElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement));

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(mockDocument)
                .setXpath("//div[@class='description_cnt--pb20 dual-bullet-list p_fix']")
                .html()
                .build();

        String expected = "<p>Bonjour</p>";

        // When
        when(mockDocument.selectXpath(searchQuery.XPath())).thenReturn(mockElements);
        when(mockElement.html()).thenReturn(expected);

        // Then
        String actual = scraper.extractDescriptionForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extract deadline works")
    void itShouldExtractDeadlineForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);
        Element mockElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement));

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(mockDocument)
                .setXpath("//span[@class='jobad-deadline-date']")
                .ownText()
                .build();

        String deadline = "12.03.2038";
        LocalDate expected = DateUtils.parseDeadline(deadline);

        // When
        when(mockDocument.selectXpath(searchQuery.XPath())).thenReturn(mockElements);
        when(mockElement.ownText()).thenReturn(deadline);

        // Then
        LocalDate actual = scraper.extractDeadlineForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting tags is not empty")
    void itShouldExtractTagsForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);
        Element mockElement = mock(Element.class);
        Element mockSiblingElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement, mockElement, mockElement));

        // When
        when(scraper.getElementsFromXPath(mockDocument, "//p[@class='txt job-tags']/a")).thenReturn(mockElements);

        when(mockElement.ownText()).thenReturn("Nøkkelord");
        when(mockElement.parent()).thenReturn(mockSiblingElement);

        when(mockSiblingElement.lastElementChild()).thenReturn(mockSiblingElement);
        when(mockSiblingElement.lastElementChild().ownText())
                .thenReturn("Lærer   , student,   heltid    ");

        // Then
        Set<String> actual = scraper.extractTagsForJobPostFromDoc(mockDocument);

        Assertions.assertFalse(actual.isEmpty());
    }

    @Test
    @DisplayName("Ensure extracting job definitions works")
    void itShouldExtractDefinitionsMapForJobPostFromDoc() {
        String parentName = "item_header";
        String childrenName = "item_cnt";
        String firstKey = "Stilling";
        String firstValue = "Heltid";
        String secondKey = "Sektor";
        String secondValue = "Privat";

        Document mockDocument = mock(Document.class);
        Element firstMockElement = mock(Element.class);
        Element firstMockSiblingElement = mock(Element.class);
        Element secondMockElement = mock(Element.class);
        Element secondMockSiblingElement = mock(Element.class);
        Elements elements = new Elements(
                List.of(firstMockElement, firstMockSiblingElement,secondMockElement, secondMockSiblingElement));

        // When
        when(scraper.getElementsFromCssQuery(mockDocument, "div.concrete_facta_item > div"))
                .thenReturn(elements);

        when(firstMockElement.hasClass(parentName)).thenReturn(true);
        when(firstMockSiblingElement.hasClass(childrenName)).thenReturn(true);

        when(secondMockElement.hasClass(parentName)).thenReturn(true);
        when(secondMockSiblingElement.hasClass(childrenName)).thenReturn(true);

        when(firstMockElement.text()).thenReturn(firstKey);
        when(firstMockSiblingElement.text()).thenReturn(firstValue);

        when(secondMockElement.text()).thenReturn(secondKey);
        when(secondMockSiblingElement.text()).thenReturn(secondValue);

        // Then
        Map<String, Set<String>> result = scraper.extractDefinitionsMapForJobPostFromDoc(mockDocument);

        int expected = elements.size() / 2;
        int actual = result.keySet().size();

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure max page is set")
    void itShouldSetMaxPage() {
        Assertions.assertNotEquals(0, scraper.getMaxPage());
    }

}
