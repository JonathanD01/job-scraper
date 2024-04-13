package no.jobbscraper.webscraper;

import no.jobbscraper.utils.ElementSearchQuery;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * A sealed abstract class providing helper methods for web scraping operations.
 * This class permits subclasses to implement specific web scraping logic.
 */
sealed abstract class BaseHelperScraper permits BaseWebScraper {
    
    private final static Logger logger = Logger.getLogger(BaseHelperScraper.class.getName());

    /**
     * Retrieves the attribute value from the specified ElementSearchQuery.
     * If the search query specifies to return text, it returns the text content of the element.
     * Otherwise, it checks if the element has all required attributes and returns the attribute value.
     *
     * @param searchQuery   The ElementSearchQuery containing the CSS selector, XPath, and attributes to search for.
     * @return              The attribute value or text content of the element, or null if not found.
     */
    protected String retrieveResultFromSearchQuery(ElementSearchQuery searchQuery) {
        Element firstElement = this.retrieveFirstElement(searchQuery);
        if (Objects.isNull(firstElement)) {
            return null;
        }

        boolean hasAllAttributes = searchQuery.requiredAttributes().stream()
                .allMatch(firstElement::hasAttr);

        if (!hasAllAttributes) {
            return null;
        }

        // Return text if query says so,
        // if not check that element
        // have all required attributes
        if (searchQuery.text()) {
            return firstElement.text();
        } else if (searchQuery.ownText()) {
            return firstElement.ownText();
        } else if (searchQuery.html()) {
            return firstElement.html();
        }
        return firstElement.attr(searchQuery.attributeToReturn());
    }

    /**
     * Retrieves the {@link Elements} matching the given XPath expression from the document.
     *
     * @param doc       The HTML document to search for elements.
     * @param XPath     The XPath expression to select elements.
     * @return          The elements matching the XPath expression.
     * @see             Elements
     */
    protected Elements getElements(Document doc, String XPath) {
        return doc.selectXpath(XPath);
    }

    /**
     * Retrieves the first {@link Element} matching the given XPath expression from the document.
     *
     * @param doc   The HTML document to search for the element.
     * @param XPath The XPath expression to select the element.
     * @return      The first element matching the XPath expression, or null if not found.
     * @see         Element
     */
    protected Element getElementFromXPath(Document doc, String XPath) {
        Elements elements = doc.selectXpath(XPath);
        return elements.first();
    }

    /**
     * Retrieves the first HTML element matching the provided CSS query from the given document.
     * If no matching element is found, returns null.
     *
     * @param doc       The HTML document to search within.
     * @param cssQuery  The CSS query used to select the desired element.
     * @return          The first HTML element matching the CSS query, or null if not found.
     */
    protected Element getElementFromCssQuery(Document doc, String cssQuery) {
        return doc.selectFirst(cssQuery);
    }

    // TODO IMPROVE
    /**
     * Retrieves the first HTML element based on the provided search query.
     * If the search query includes both CSS and XPath queries, CSS query is prioritized.
     * If no matching element is found or the search query is not properly configured, returns null.
     *
     * @param searchQuery   The ElementSearchQuery containing the necessary details for element retrieval.
     * @return              The first HTML element matching the search query, or null if not found or improperly configured.
     */
    protected Element retrieveFirstElement(ElementSearchQuery searchQuery) {
        Document document = searchQuery.document();
        String cssQuery = searchQuery.cssQuery();
        String XPath = searchQuery.XPath();

        if (Objects.nonNull(document)) {
            if (Objects.nonNull(cssQuery)) {
                return retrieveFirstElementFromCssQueryWithDocument(searchQuery);
            }
            return retrieveFirstElementFromXPathWithDocument(searchQuery);
        }

        if (Objects.nonNull(cssQuery)) {
            return retrieveFirstElementFromCssQuery(searchQuery);
        }

        if (Objects.nonNull(XPath)) {
            return retrieveFirstElementFromXPath(searchQuery);
        }

        logger.severe("ElementSearchQuery object was not setup properly for " + searchQuery.url());
        return null;
    }

    /**
     * Retrieves the first HTML element based on the provided CSS query from the search query.
     * If no matching element is found, returns null and logs a warning message.
     *
     * @param searchQuery   The ElementSearchQuery containing the necessary details for element retrieval.
     * @return              The first HTML element matching the CSS query, or null if not found.
     */
    private Element retrieveFirstElementFromCssQuery(ElementSearchQuery searchQuery){
        try {
            return searchQuery.element().expectFirst(searchQuery.cssQuery());
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get first element with css query: " + searchQuery.cssQuery() +
                            " at " + searchQuery.url());
            return null;
        }
    }

    /**
     * Retrieves the first HTML element based on the provided XPath from the search query.
     * If no matching element is found, returns null and logs a warning message.
     *
     * @param searchQuery   The ElementSearchQuery containing the necessary details for element retrieval.
     * @return              The first HTML element matching the XPath, or null if not found.
     */
    private Element retrieveFirstElementFromXPath(ElementSearchQuery searchQuery){
        try {
            return searchQuery.element().selectXpath(searchQuery.XPath()).first();
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get first element with XPath: " + searchQuery.XPath() +
                            " at " + searchQuery.url());
            return null;
        }
    }

    /**
     * Retrieves the first HTML element based on the provided CSS query from the search query's document.
     * If no matching element is found, returns null and logs a warning message.
     *
     * @param searchQuery   The ElementSearchQuery containing the necessary details for element retrieval.
     * @return              The first HTML element matching the CSS query, or null if not found.
     */
    protected Element retrieveFirstElementFromCssQueryWithDocument(ElementSearchQuery searchQuery){
        try {
            return getElementFromCssQuery(searchQuery.document(), searchQuery.cssQuery());
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get first element with css query: " + searchQuery.cssQuery() +
                    " at " + searchQuery.url());
            return null;
        }
    }

    /**
     * Retrieves the first HTML element based on the provided XPath expression from the search query's document.
     * If no matching element is found, returns null and logs a warning message.
     *
     * @param searchQuery   The ElementSearchQuery containing the necessary details for element retrieval.
     * @return              The first HTML element matching the XPath expression, or null if not found.
     */
    protected Element retrieveFirstElementFromXPathWithDocument(ElementSearchQuery searchQuery){
        try {
            return getElementFromXPath(searchQuery.document(), searchQuery.XPath());
        } catch (IllegalArgumentException e) {
            logger.warning("Failed to get first element with XPath: " + searchQuery.XPath() +
                    " at " + searchQuery.url());
            return null;
        }
    }

}

