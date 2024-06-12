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

public class FinnScraperTest {

    private static BaseWebScraper scraper;

    @BeforeAll
    public static void setUp() {
        scraper = new FinnScraper();
    }

    @Test
    @DisplayName("Ensure extracting url works")
    public void itShouldExtractUrlForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Element mockElement = mock(Element.class);

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, mockElement)
                .setCssQuery("h2 > a[href]")
                .setRequiredAttributes(List.of("id"))
                .attributeToReturn("abs:href")
                .build();

        String expected = url + "/job/123";

        // When
        when(mockElement.expectFirst(searchQuery.cssQuery())).thenReturn(mockElement);
        when(mockElement.hasAttr("id")).thenReturn(true);
        when(mockElement.attr(searchQuery.attributeToReturn())).thenReturn(expected);

        // Then
        String actual = scraper.extractUrlForJobPostFromElement(url, mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure image url is present")
    void itShouldExtractImageUrlForJobPostFromElement() {
        // Given
        String url = scraper.getCurrentUrl();
        Element mockElement = mock(Element.class);

        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, mockElement)
                .setCssQuery("img[src]")
                .attributeToReturn("abs:src")
                .build();

        String expected = "https://images.finncdn.no/dynamic/480w/";

        // When
        when(mockElement.expectFirst(searchQuery.cssQuery())).thenReturn(mockElement);
        when(mockElement.attr(searchQuery.attributeToReturn())).thenReturn(expected);

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
                .setCssQuery("h2")
                .text()
                .build();

        String expected = "Senior utvikler hos Finn!";

        // When
        when(scraper.retrieveFirstElement(searchQuery)).thenReturn(mockElement);
        when(mockElement.text()).thenReturn(expected);

        // Then
        String actual = scraper.extractTitleForJobPostFromElement(url, mockElement);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting company name works")
    void itShouldExtractCompanyNameForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);
        Element mockElement = mock(Element.class);
        Element mockElementFirstElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement));

        String expected = "Tencent";

        // When
        when(scraper.getElementsFromXPath(mockDocument, "//ul/li"))
                .thenReturn(mockElements);

        when(mockElement.firstElementChild()).thenReturn(mockElementFirstElement);

        when(mockElementFirstElement.ownText()).thenReturn("arbeidsgiver");

        when(mockElement.ownText()).thenReturn(expected);

        // Then
        String actual = scraper.extractCompanyNameForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting company image url is empty")
    void itShouldExtractCompanyImageUrlForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(mockDocument)
                .setXPath("//img[@class='img-format__img']")
                .attributeToReturn("abs:src")
                .build();

        Element mockFirstElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockFirstElement));

        String expected = "https://images.finncdn.no/dynamic/1600w/logo/logo";

        // When
        when(mockDocument.selectXpath(searchQuery.XPath())).thenReturn(mockElements);
        when(mockFirstElement.attr(searchQuery.attributeToReturn())).thenReturn(expected);

        // Then
        String actual = scraper.extractCompanyImageUrlForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting description works")
    void itShouldExtractDescriptionForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(mockDocument)
                .setCssQuery("div.import-decoration")
                .build();

        Element mockElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement));

        String expected = "<p>Bonjour</p>";

        // When
        when(mockDocument.select(searchQuery.cssQuery())).thenReturn(mockElements);
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
        Element mockElementFirstElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement));

        String deadlineText = "12.03.2038";
        LocalDate expected = DateUtils.parseDeadline(deadlineText);

        // When
        when(scraper.getElementsFromXPath(mockDocument, "//ul/li"))
                .thenReturn(mockElements);

        when(mockElement.firstElementChild()).thenReturn(mockElementFirstElement);

        when(mockElementFirstElement.ownText()).thenReturn("frist");


        when(mockElement.ownText()).thenReturn(deadlineText);

        // Then
        LocalDate actual = scraper.extractDeadlineForJobPostFromDoc(mockDocument);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Ensure extracting tags is empty")
    void itShouldExtractTagsForJobPostFromDoc() {
        // Given
        Document mockDocument = mock(Document.class);
        Element mockElement = mock(Element.class);
        Element mockSiblingElement = mock(Element.class);
        Elements mockElements = new Elements(List.of(mockElement, mockElement, mockElement));

        // When
        when(scraper.getElementsFromXPath(mockDocument, "//section[@class='panel']/h2[@class='u-t3']")).thenReturn(mockElements);
        when(mockElement.ownText()).thenReturn("Nøkkelord");
        when(mockElement.parent()).thenReturn(mockSiblingElement);
        when(mockSiblingElement.lastElementChild()).thenReturn(mockSiblingElement);
        when(mockSiblingElement.lastElementChild().ownText())
                .thenReturn("Lærer   , student,   heltid    ");

        // Then
        Set<String> expected = scraper.extractTagsForJobPostFromDoc(mockDocument);

        Assertions.assertFalse(expected.isEmpty());
    }

    @Test
    @DisplayName("Ensure extraction description map works")
    void itShouldExtractDescriptionMapForJobPostFromDoc() {
        // Given
        String tagName = "li";

        String firstKey = "Stilling";
        String firstValue = "Heltid";

        String secondKey = "Sektor";
        String secondValue = "Privat";

        String thirdKey = "Sted";
        String thirdValue = "Oslo";

        String fourthKey = "Bransje";
        String fourthValue = "IT";

        Document mockDocument = mock(Document.class);

        Element firstMockElement = mock(Element.class);
        Element secondMockElement = mock(Element.class);
        Element thirdMockElement = mock(Element.class);
        Element fourthMockElement = mock(Element.class);

        Element firstSiblingMockElement = mock(Element.class);
        Element secondSiblingMockElement = mock(Element.class);
        Element thirdSiblingMockElement = mock(Element.class);
        Element fourthSiblingMockElement = mock(Element.class);

        Elements elements = new Elements(
                List.of(firstMockElement, secondMockElement, thirdMockElement, fourthMockElement));

        // When
        when(scraper.getElementsFromXPath(mockDocument, "//ul/li"))
                .thenReturn(elements);

        when(firstMockElement.tagName()).thenReturn(tagName);
        when(secondMockElement.tagName()).thenReturn(tagName);
        when(thirdMockElement.tagName()).thenReturn(tagName);
        when(fourthMockElement.tagName()).thenReturn(tagName);

        when(firstMockElement.firstElementChild()).thenReturn(firstSiblingMockElement);
        when(secondMockElement.firstElementChild()).thenReturn(secondSiblingMockElement);
        when(thirdMockElement.firstElementChild()).thenReturn(thirdSiblingMockElement);
        when(fourthMockElement.firstElementChild()).thenReturn(fourthSiblingMockElement);

        when(firstMockElement.ownText()).thenReturn(firstValue);
        when(secondMockElement.ownText()).thenReturn(secondValue);
        when(thirdMockElement.ownText()).thenReturn(thirdValue);
        when(fourthMockElement.ownText()).thenReturn(fourthValue);

        when(firstSiblingMockElement.ownText()).thenReturn(firstKey);
        when(secondSiblingMockElement.ownText()).thenReturn(secondKey);
        when(thirdSiblingMockElement.ownText()).thenReturn(thirdKey);
        when(fourthSiblingMockElement.ownText()).thenReturn(fourthKey);

        // Then
        Map<String, Set<String>> result = scraper.extractDefinitionsMapForJobPostFromDoc(mockDocument);

        int expected = elements.size();
        int actual = result.keySet().size();

        Assertions.assertEquals(expected, actual);
    }

}
