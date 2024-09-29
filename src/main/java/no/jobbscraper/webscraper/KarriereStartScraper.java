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
        super("karrierestart", WebsiteURL.KARRIERESTART_NO, WebsiteURL.KARRIERESTART_NO_WITH_PAGE, "//div[@class='featured-wrap']");
        this.setMaxPage();
    }

    KarriereStartScraper(int maxPage) {
        super("karrierestart", WebsiteURL.KARRIERESTART_NO, WebsiteURL.KARRIERESTART_NO_WITH_PAGE, "//div[@class='featured-wrap']");
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

        String retrievedUrl = retrieveResultFromSearchQuery(searchQuery);

        if (retrievedUrl == null) {
            throw new NullPointerException("Retrieved url was null from " + url);
        }

        return retrievedUrl;
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

        String retrievedTitle = retrieveResultFromSearchQuery(searchQuery);

        if (retrievedTitle == null) {
            throw new NullPointerException("Title was null from " + url);
        }

        return retrievedTitle;
    }

    @Override
    String extractCompanyNameForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXPath("//div[@class='menu-item topic-header-text']")
                .ownText()
                .build();

        String retrievedCompanyName = retrieveResultFromSearchQuery(searchQuery);

        if (retrievedCompanyName == null) {
            throw new NullPointerException("Company name was null from : " + doc.location());
        }

        return retrievedCompanyName;
    }

    @Override
    String extractCompanyImageUrlForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXPath("//div[@class='cp_header_logo']/a/img")
                .attributeToReturn("abs:src")
                .build();

        return retrieveResultFromSearchQuery(searchQuery);
    }

    @Override
    String extractDescriptionForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXPath("//div[@class='jobad-info-block dual-bullet-list p_fix']")
                .html()
                .build();

        String retrievedDescription = retrieveResultFromSearchQuery(searchQuery);

        if (retrievedDescription == null) {
            searchQuery = new ElementSearchQuery.Builder(doc)
                .setXPath("//div[@class='description_cnt']")
                .html()
                .build();

            retrievedDescription = retrieveResultFromSearchQuery(searchQuery);
        }

        if (retrievedDescription == null){
            throw new NullPointerException("Job description was null from: " + doc.location());
        }

        return retrievedDescription;
    }

    @Override
    LocalDate extractDeadlineForJobPostFromDoc(Document doc) {
        ElementSearchQuery searchQuery = new ElementSearchQuery.Builder(doc)
                .setXPath("//span[@class='jobad-deadline-date']")
                .ownText()
                .build();

        String dateAsText = retrieveResultFromSearchQuery(searchQuery);
        return DateUtils.parseDeadline(dateAsText);
    }

    @Override
    Set<String> extractTagsForJobPostFromDoc(Document doc) {
        return this.getElementsFromXPath(doc, "//p[@class='txt job-tags']/a").stream()
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

        Elements thElements = getElementsFromXPath(doc, "//table/tbody/tr/th");
        Elements spanElements = getElementsFromXPath(doc, "//table/tbody/tr/td/span");


        if (thElements.isEmpty()) {
            logger.severe("Elements at //table/tbody/tr/th were empty");
            return definitionMap;
        }

        if (spanElements.isEmpty()){
            logger.severe("Elements at //table/tbody/tr/td were empty");
            return definitionMap;
        }

        int counter = -1;
        for (Element thElement : thElements) {
            counter += 1;

            Element spanElement = spanElements.get(counter);

            String spanText = null;
            if (spanElement.hasText()){
                spanText = spanElement.text();
            } else if (!spanElement.children().isEmpty()){
                Element firstChild = spanElement.children().getFirst();
                spanText = firstChild.text();
            }

            if (spanText == null) {
                continue;
            }

            String currentItemHeaderText = retrieveProperDefinitionName(thElement.wholeText());

            if (Objects.isNull(currentItemHeaderText)) {
                continue;
            }

            Set<String> definitions = definitionMap.getOrDefault(currentItemHeaderText, new HashSet<>());

            String value = retrieveCorrectValueForKey(currentItemHeaderText,
                    StringUtils.removeTrailingComma(spanText));
            definitions.add(value);
            definitionMap.put(currentItemHeaderText, definitions);
        }
        return definitionMap;
    }

    @Override
    protected void setMaxPage(){
        Document doc = this.getDocument(getCurrentUrl());

        Elements liElements = this.getElementsFromXPath(doc, "//ul[@class='paginate paginate-mobile']/li");
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
