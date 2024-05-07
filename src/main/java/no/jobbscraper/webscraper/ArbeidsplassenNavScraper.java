package no.jobbscraper.webscraper;

import no.jobbscraper.url.WebsiteURL;
import no.jobbscraper.utils.DateUtils;
import no.jobbscraper.utils.ElementSearchQuery;
import no.jobbscraper.utils.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.*;

public final class ArbeidsplassenNavScraper extends BaseWebScraper {

    private final int elementsPerPage;

    public ArbeidsplassenNavScraper() {
        super("arbeidsplassennav", WebsiteURL.ARBEIDSPLASSEN_NAV_NO, WebsiteURL.ARBEIDSPLASSEN_NAV_NO_WITH_PAGE, "//article");
        this.elementsPerPage = 25;
    }

    /**
     * Gets the page number for the URL.
     * For Arbeidsplassen nav, the page number increases by 25 instead of incrementing
     * by 1 like traditional pagination.
     * To navigate to the next page on Arbeidsplassen nav, the page number must be
     * incremented by page * 25.
     *
     * @return The current page number.
     */
    @Override
    public int getPage() {
        return super.getPage() * elementsPerPage;
    }

    @Override
    String extractUrlForJobPostFromElement(String url, Element element) {
        // TODO Why is sibling index needed?
        int elementSiblingIndex = element.elementSiblingIndex() + 1;
        String XPath = String.format("//article[%s]/div[1]/div[1]/div/h3/a", elementSiblingIndex);
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setXpath(XPath)
                .attributeToReturn("abs:href")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractImageUrlForJobPostFromElement(String url, Element element) {
        // Site have no image
        return "";
    }

    @Override
    String extractTitleForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("h3.overflow-wrap-anywhere.navds-heading.navds-heading--small > a.navds-link.navds-link--action")
                .text()
                .setRequiredAttributes(List.of("href"))
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractCompanyNameForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("//p[@class='navds-body-long navds-body-long--medium navds-typo--semibold']")
                .ownText()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractCompanyImageUrlForJobPostFromDoc(Document doc) {
        // Site have no image
        return "";
    }

    @Override
    String extractDescriptionForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("//div[@class='arb-rich-text job-posting-text']")
                .html()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    LocalDate extractDeadlineForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("/html/body/div/div/main/div/article/div/div[2]/div/dl/dd/p")
                .ownText()
                .build();

        String dateAsText = retrieveResultFromSearchQuery(searchQuery);
        return DateUtils.parseDeadline(dateAsText);
    }

    @Override
    Set<String> extractTagsForJobPostFromDoc(Document doc) {
        return Collections.emptySet();
    }

    @Override
    Map<String, Set<String>> extractDefinitionsMapForJobPostFromDoc(Document doc) {
        Map<String, Set<String>> definitionMap = new HashMap<>();

        // Loop through all elements
        // If an element is found, and it has the required class/tag
        // then grab the elements own text and add it under as value for
        // the elements own text as key. Only do if the value element have
        // the required class

        String XPath = "/html/body/div/div/main/article/div/section[2]/dl";
        Elements elements = getElementsFromXPath(doc, XPath);
        if (elements.isEmpty()) {
            logger.severe("Elements " + XPath + " were empty");
            return definitionMap;
        }

        Element currentItemHeader = null;
        for (Element elementInDoc : elements) {
            if (elementInDoc.hasClass("navds-label")) {
                currentItemHeader = elementInDoc;
            }

            String elementsOwnText = elementInDoc.ownText();

            if (StringUtils.isEmpty(elementsOwnText) || Objects.isNull(currentItemHeader)) {
                continue;
            }

            String currentItemHeaderText = retrieveProperDefinitionName(currentItemHeader.ownText());

            if (StringUtils.isEmpty(currentItemHeaderText)) {
                continue;
            }

            Set<String> definitions = definitionMap.getOrDefault(currentItemHeaderText, new HashSet<>());

            String value = retrieveCorrectValueForKey(currentItemHeaderText,
                    StringUtils.removeTrailingComma(elementsOwnText));
            definitions.add(value);
            definitionMap.put(currentItemHeaderText, definitions);
        }
        return definitionMap;
    }
}
