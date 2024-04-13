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

public final class FinnScraper extends BaseWebScraper {


    public FinnScraper() {
        super(WebsiteURL.FINN_NO, WebsiteURL.FINN_NO_WITH_PAGE, "//article");
    }

    @Override
    String extractUrlForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("h2 > a[href]")
                .setRequiredAttributes(List.of("id"))
                .attributeToReturn("abs:href")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractImageUrlForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("img[src]")
                .attributeToReturn("abs:src")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractTitleForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("h2")
                .text()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractCompanyNameForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setCssQuery("h2")
                .ownText()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractCompanyImageUrlForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("//img[@class='img-format__img']")
                .attributeToReturn("abs:src")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractDescriptionForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("/html/body/main/div/div[3]/div[1]/div/div[3]/section")
                .html()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    LocalDate extractDeadlineForJobPostFromDoc(Document doc) {
        Elements elements = this.getElements(doc, "//dl[@class='definition-list']/dt");
        if (elements.isEmpty()) {
            logger.warning("Elements list was empty, could not get deadline for job post at " + doc.location());
            return null;
        }

        for (Element element : elements) {
            if (element.ownText().equalsIgnoreCase("frist")) {
                String dateAsText = element.nextElementSibling().ownText();
                return DateUtils.parseDeadline(dateAsText);
            }
        }

        logger.warning("Could not get deadline for job post at " + doc.location());
        return null;
    }

    @Override
    Set<String> extractTagsForJobPostFromDoc(Document doc) {
        Elements h2Elements = this.getElements(doc, "//section[@class='panel']/h2[@class='u-t3']");

        for (Element h2Element : h2Elements) {
            if (StringUtils.removeWhitespace(h2Element.ownText()).equalsIgnoreCase("Nøkkelord")) {
                Element parentElement = h2Element.parent();

                String tagString = parentElement.lastElementChild().ownText().replaceAll(" ", "");
                String fullStringTag = StringUtils.removeTrailingComma(tagString);
                String[] tags = fullStringTag.split(",");
                return Set.copyOf(Arrays.asList(tags));
            }
        }
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
        Element currentItemHeader = null;
        for (Element elementInDoc : doc.getAllElements()) {
            if (elementInDoc.tagName().equalsIgnoreCase("dt")) {
                currentItemHeader = elementInDoc;
            }

            String elementsOwnText = StringUtils.removeTrailingComma(elementInDoc.ownText());

            if (StringUtils.isEmpty(elementsOwnText)) {
                continue;
            }

            if (Objects.isNull(currentItemHeader) || !elementInDoc.tagName().equalsIgnoreCase("dd")) {
                continue;
            }

            String currentItemHeaderText = retrieveProperDefinitionName(currentItemHeader.ownText());

            if (Objects.isNull(currentItemHeaderText)) {
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
