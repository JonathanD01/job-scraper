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
        super("finn", WebsiteURL.FINN_NO, WebsiteURL.FINN_NO_WITH_PAGE, "//article");
    }

    @Override
    String extractUrlForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("h2 > a[href]")
                .setRequiredAttributes(List.of("id"))
                .attributeToReturn("abs:href")
                .build();

        String retrievedUrl = retrieveResultFromSearchQuery(searchQuery);

        if (retrievedUrl == null) {
            throw new NullPointerException("Retrieved url was null from " + url);
        }

        return retrievedUrl;
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

        String retrievedTitle = retrieveResultFromSearchQuery(searchQuery);

        if (retrievedTitle == null) {
            throw new NullPointerException("Title was null from " + url);
        }

        return retrievedTitle;
    }

    @Override
    String extractCompanyNameForJobPostFromDoc(Document doc) {
        Elements elements = this.getElementsFromXPath(doc, "//ul/li");
        if (elements.isEmpty()) {
            logger.warning("Elements list was empty, could not get company name for job post at " + doc.location());
            return null;
        }

        for (Element element : elements) {
            Element firstElementChild = element.firstElementChild();
            boolean isLiElementFrist = firstElementChild != null &&
                    firstElementChild
                            .ownText()
                            .replaceAll(":", "")
                            .equalsIgnoreCase("arbeidsgiver");
            if (isLiElementFrist) {
                return element.ownText();
            }
        }

        throw new NullPointerException("Company name was null from : " + doc.location());
    }

    @Override
    String extractCompanyImageUrlForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXPath("//img[@class='img-format__img']")
                .attributeToReturn("abs:src")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractDescriptionForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setCssQuery("div.import-decoration")
                .build();

        StringBuilder stringBuilder = new StringBuilder();

        Elements descriptionElements = getElementsFromCssQuery(searchQuery.document(), searchQuery.cssQuery());

        if (descriptionElements.isEmpty()) {
            throw new NullPointerException("Job description was null from : " + doc.location());
        }

        descriptionElements.forEach(element -> stringBuilder.append(element.html()));

        return stringBuilder.toString();
    }

    @Override
    LocalDate extractDeadlineForJobPostFromDoc(Document doc) {
        Elements elements = this.getElementsFromXPath(doc, "//ul/li");
        if (elements.isEmpty()) {
            logger.warning("Elements list was empty, could not get deadline for job post at " + doc.location());
            return null;
        }

        for (Element element : elements) {
            Element firstElementChild = element.firstElementChild();
            boolean isLiElementFrist = firstElementChild != null &&
                    firstElementChild
                            .ownText()
                            .replaceAll(":", "")
                            .equalsIgnoreCase("frist");
            if (isLiElementFrist) {
                String dateAsText = element.ownText();
                return DateUtils.parseDeadline(dateAsText);
            }
        }

        logger.warning("Could not get deadline for job post at " + doc.location());
        return null;
    }

    @Override
    Set<String> extractTagsForJobPostFromDoc(Document doc) {
        Elements h2Elements = this.getElementsFromXPath(doc, "//section[@class='panel']/h2[@class='u-t3']");

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

        String XPath = "//ul/li";
        Elements elements = getElementsFromXPath(doc, XPath);
        if (elements.isEmpty()) {
            logger.severe("Elements at " + XPath + " were empty");
            return definitionMap;
        }

        for (Element elementInDoc : elements) {
            if (elementInDoc.tagName().equalsIgnoreCase("li")) {
                Element currentItemHeader = elementInDoc.firstElementChild();
                if (currentItemHeader == null) {
                    continue;
                }

                String elementsOwnText = elementInDoc.ownText();

                if (StringUtils.isEmpty(elementsOwnText)) {
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
        }
        return definitionMap;
    }

}
