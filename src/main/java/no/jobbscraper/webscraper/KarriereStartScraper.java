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
import java.util.stream.Collectors;

public final class KarriereStartScraper extends BaseWebScraper {

    public KarriereStartScraper() {
        super(WebsiteURL.KARRIERESTART_NO, WebsiteURL.KARRIERESTART_NO_WITH_PAGE, "//div[@id='f-search-results']/div");
        this.setMaxPage();
    }

    KarriereStartScraper(int maxPage) {
        super(WebsiteURL.KARRIERESTART_NO, WebsiteURL.KARRIERESTART_NO_WITH_PAGE, "//div[@id='f-search-results']/div");
        super.setMaxPage(maxPage);
    }

    @Override
    public boolean continueScan() {
        return super.continueScan() && getPage() <= getMaxPage();
    }

    @Override
    String extractUrlForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("a.j-title")
                .attributeToReturn("abs:href")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractImageUrlForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("div.j-presentation.j-presentation-overflowed > a > img[src]")
                .attributeToReturn("abs:src")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractTitleForJobPostFromElement(String url, Element element) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(url, element)
                .setCssQuery("a.j-title > span")
                .text()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractCompanyNameForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("//div[@class='menu-item topic-header-text']")
                .ownText()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractCompanyImageUrlForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("//div[@class='cp_header_logo']/a/img")
                .attributeToReturn("abs:src")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractDescriptionForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("//div[@class='description_cnt--pb20 dual-bullet-list p_fix']")
                .html()
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    LocalDate extractDeadlineForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXpath("//span[@class='jobad-deadline-date']")
                .ownText()
                .build();


        String dateAsText = retrieveResultFromSearchQuery(searchQuery);
        return DateUtils.parseDeadline(dateAsText);
    }

    @Override
    Set<String> extractTagsForJobPostFromDoc(Document doc) {
        return this.getElements(doc, "//p[@class='txt job-tags']/a").stream()
                .map(element -> StringUtils.removeTrailingComma(element.ownText()))
                .collect(Collectors.toSet());
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
            if (elementInDoc.hasClass("item_header")) {
                currentItemHeader = elementInDoc;
            }

            String elementsOwnText = elementInDoc.ownText();

            if (Objects.isNull(currentItemHeader) || !elementInDoc.hasClass("item_cnt")) {
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

    @Override
    protected void setMaxPage(){
        Document doc = this.getDocument(getCurrentUrl());

        Elements liElements = this.getElements(doc, "//ul[@class='paginate paginate-mobile']/li");
        Element lastPageElement = liElements.last().select("a[href]").first();
        if (lastPageElement == null) {
            logger.severe("Could not setup max page. " + doc.location());
            setContinueScan(false);
            return;
        }

        String hrefAttribute = lastPageElement.attr("abs:href");
        try {
            int maxPage = Integer.parseInt(hrefAttribute.split("page=")[1].replaceAll(" ", ""));
            setMaxPage(maxPage);
            logger.info("Set max page for to " + maxPage);
        } catch (NumberFormatException e) {
            logger.severe("Could not format the string '" + hrefAttribute + "' to an integer after splitting on 'page=[1]'");
        }
    }
}
