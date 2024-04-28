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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsplassenNavScraperTest {

    private static BaseWebScraper scraper;

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
        Element mockElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement));

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, mockElement)
                .setXpath("anyString()")
                .attributeToReturn("abs:href")
                .build();

        String expected = url + "/job/123";

        // When
        // Needs anyString()
        when(mockElement.selectXpath(anyString())).thenReturn(mockElements);
        when(mockElement.attr(searchQuery.attributeToReturn())).thenReturn(expected);

        // Then
        String actual = scraper.extractUrlForJobPostFromElement(url, mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure image url is empty")
    void itShouldExtractImageUrlForJobPostFromElement() {
        // Given
        Element mockElement = mock(Element.class);

        String expected = "";

        // When
        // Then
        String actual = scraper.extractImageUrlForJobPostFromElement(scraper.getCurrentUrl(), mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting title works")
    void itShouldExtractTitleForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Element mockElement = mock(Element.class);

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, mockElement)
                .setCssQuery("h3.overflow-wrap-anywhere.navds-heading.navds-heading--small > a.navds-link.navds-link--action")
                .text()
                .setRequiredAttributes(List.of("href"))
                .build();

        String expected = "Senior utvikler hos Eksempel";

        // When
        when(scraper.retrieveFirstElement(searchQuery)).thenReturn(mockElement);
        when(mockElement.hasAttr("href")).thenReturn(true);
        when(mockElement.text()).thenReturn(expected);

        // Then
        String actual = scraper.extractTitleForJobPostFromElement(url, mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting company name works")
    void itShouldExtractCompanyNameForJobPostFromDoc() {
        // Given
        String expected = "Big Business";

        Document mockDocument = mock(Document.class);
        Element mockFirstElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockFirstElement));

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(mockDocument)
                .setXpath("//p[@class='navds-body-long navds-body-long--medium navds-typo--semibold']")
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
        Document mockDocument = mock(Document.class);

        String expected = "";

        // When
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
                .setXpath("//div[@class='arb-rich-text job-posting-text']")
                .html()
                .build();

        String expected = "<p>Hello</p>";

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
                .setXpath("/html/body/div/div/main/div/article/div/div[2]/div/dl/dd/p")
                .ownText()
                .build();

        String deadline = "12.03.2030";
        LocalDate expected = DateUtils.parseDeadline(deadline);

        // When
        when(mockDocument.selectXpath(searchQuery.XPath())).thenReturn(mockElements);
        when(mockElement.ownText()).thenReturn(deadline);

        // Then
        LocalDate actual = scraper.extractDeadlineForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting tags is empty")
    void itShouldExtractTagsForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);

        List<String> expected = Collections.emptyList();

        // When
        // Then
        Set<String> actual = scraper.extractTagsForJobPostFromDoc(mockDocument);

        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extraction description map works")
    void itShouldExtractDescriptionMapForJobPostFromDoc() {
        // Given
        String parentName = "navds-label";
        String childrenName = "navds-body-long navds-body-long--medium";
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
        when(scraper.getElementsFromXPath(mockDocument, "/html/body/div/div/main/article/section[2]/dl"))
                .thenReturn(elements);

        when(firstMockElement.hasClass(parentName)).thenReturn(true);
        when(firstMockSiblingElement.hasClass(childrenName)).thenReturn(true);

        when(secondMockElement.hasClass(parentName)).thenReturn(true);
        when(secondMockSiblingElement.hasClass(childrenName)).thenReturn(true);

        when(firstMockElement.ownText()).thenReturn(firstKey);
        when(firstMockSiblingElement.ownText()).thenReturn(firstValue);

        when(secondMockElement.ownText()).thenReturn(secondKey);
        when(secondMockSiblingElement.ownText()).thenReturn(secondValue);

        // Then
        Map<String, Set<String>> result = scraper.extractDefinitionsMapForJobPostFromDoc(mockDocument);

        int expected = elements.size() / 2;
        int actual = result.keySet().size();

        Assertions.assertEquals(expected, actual);
    }

}
