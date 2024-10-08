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
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXPath("/html/body/main/div[2]/article/section[1]/section[2]/div/p")
                .build();

        Element element = this.retrieveFirstElement(searchQuery);

        String companyName = element.ownText();
        if (companyName != null) {
            return companyName;
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
            boolean hasChild = firstElementChild != null &&
                    element.ownText().equalsIgnoreCase("frist") &&
                    firstElementChild.hasText();
            if (hasChild) {
                String dateAsText = firstElementChild.ownText();
                return DateUtils.parseDeadline(dateAsText);
            }
        }

        logger.warning("Could not get deadline for job post at " + doc.location());
        return null;
    }

    @Override
    Set<String> extractTagsForJobPostFromDoc(Document doc) {
        Element keywordElement = this.getElementFromCssQuery(doc, "section > h2.t3 + p");

        if (keywordElement == null || !keywordElement.hasText()) {
            return Collections.emptySet();
        }

        String tagString = keywordElement.ownText().replaceAll(" ", "");

        String fullStringTag = StringUtils.removeTrailingComma(tagString);
        assert fullStringTag != null;
        String[] tags = fullStringTag.split(",");
        return Set.copyOf(Arrays.asList(tags));
    }

    @Override
    Map<String, Set<String>> extractDefinitionsMapForJobPostFromDoc(Document doc) {
        Map<String, Set<String>> definitionMap = new HashMap<>();

        // Loop through all elements
        // If an element is found, and it has the required class/tag
        // then grab the elements own text and add it under as value for
        // the elements own text as key. Only do if the value element have
        // the required class

        String XPath = "//ul[@class='space-y-10 ']/li";
        Elements elements = getElementsFromXPath(doc, XPath);
        if (elements.isEmpty()) {
            logger.severe("Elements at " + XPath + " were empty");
            return definitionMap;
        }

        for (Element elementInDoc : elements) {

            Elements children = elementInDoc.children();

            for (Element currentItemHeader : children) {
                if (!currentItemHeader.tagName().equalsIgnoreCase("span")) {
                    continue;
                }

                StringJoiner siblingElementsText = new StringJoiner(", ");

                currentItemHeader.siblingElements().forEach(element -> siblingElementsText.add(element.ownText()));

                int childrenCount = currentItemHeader.siblingElements().size();

                String elementsOwnText;

                if (childrenCount == 0 && currentItemHeader.parent() != null) {
                    elementsOwnText = siblingElementsText + currentItemHeader.parent().ownText();
                } else if (childrenCount == 1 && currentItemHeader.parent() != null) {
                    elementsOwnText = siblingElementsText + currentItemHeader.parent().ownText();
                } else {
                    elementsOwnText = siblingElementsText.toString();
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
