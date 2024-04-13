package no.jobbscraper.webscraper;

/**
 * A sealed interface representing a web scraper.
 * Only implementations permitted by {@link BaseWebScraper} are allowed.
 */
sealed interface IWebScraper permits BaseWebScraper {

    /**
     * Scans the website to extract relevant data.
     * Implementations of this method should define the scraping logic.
     */
    void scan();

}
