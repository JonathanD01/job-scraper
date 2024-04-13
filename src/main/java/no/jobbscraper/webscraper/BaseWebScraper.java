package no.jobbscraper.webscraper;

import no.jobbscraper.argument.Argument;
import no.jobbscraper.database.Database;
import no.jobbscraper.jobpost.JobPost;
import no.jobbscraper.restapiclient.BaseRestApiClient;
import no.jobbscraper.url.WebsiteURL;
import no.jobbscraper.utils.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sealed abstract class serving as the base for web scrapers.
 * It extends {@link BaseHelperScraper} and implements {@link IWebScraper}.
 * Subclasses must be permitted by this class and are allowed to implement specific scraping logic.
 * Permitted subclasses include {@link ArbeidsplassenNavScraper}, {@link FinnScraper}, and {@link KarriereStartScraper}.
 */
public sealed abstract class BaseWebScraper
        extends BaseHelperScraper
        implements IWebScraper
        permits ArbeidsplassenNavScraper,FinnScraper, KarriereStartScraper {

    private static final Map<String, String> definitionMap = Map.ofEntries(
            Map.entry("adresse", "Sted"),
            Map.entry("ansettelsesform", "Ansettelsesform"),
            Map.entry("antall stillinger", "Stillinger"),
            Map.entry("arbeidsdager", "Arbeidsdager"),
            Map.entry("arbeidsgiver", "Arbeidsgiver"),
            Map.entry("arbeidsspråk", "Arbeidsspråk"),
            Map.entry("arbeidstid", "Arbeidstid"),
            Map.entry("arbeidstidsordning", "Arbeidstidsordning"),
            Map.entry("bransje", "Bransje"),
            Map.entry("heltid/deltid", "Stilling"),
            Map.entry("hjemmekontor", "Hjemmekontor"),
            Map.entry("lederkategori", "Lederkategori"),
            Map.entry("oppstart", "Oppstart"),
            Map.entry("sektor", "Sektor"),
            Map.entry("sted", "Sted"),
            Map.entry("stillingsfunksjon", "Stillingsfunksjon"),
            Map.entry("stillingstittel", "Stillingstittel"),
            Map.entry("stillingstype", "Stilling"),
            Map.entry("stilling", "Stilling")
    );

    protected static final Logger logger = Logger.getLogger(BaseWebScraper.class.getName());
    protected static final BaseRestApiClient apiClient = BaseRestApiClient.getInstance(false);
    private final static int CONNECT_TRIES = 3;
    private final static int WAIT_BEFORE_RECONNECT_MILLIS = 5000;
    private final String url;
    private final String urlWithPageQuery;
    private final String XPath;
    private int page;
    private int maxPage;
    private boolean continueScan;

    /**
     * Constructs a BaseWebScraper object with the specified WebsiteURL.
     *
     * @param url                       a website url of the website to scrape
     * @param urlWithPageQuery          a website url of the website to scrape with page query
     * @param XPath                     a string representing the XPath to the "job posting cards"
     */
    public BaseWebScraper(WebsiteURL url, WebsiteURL urlWithPageQuery, String XPath) {
        this.url = url.get();
        this.urlWithPageQuery = urlWithPageQuery.get();
        this.XPath = XPath;
        this.page = 1;
        this.maxPage = 0;
        this.continueScan = true;
    }

    @Override
    public void scan() {
        // Check if scraper is disabled
        if (isScraperDisabled()) {
            logger.info("Scraper " + getClass().getName() + " is disabled...");
            return;
        }

        // Check if page argument was provided
        setStartPageFromArgument();

        while (continueScan()) {
            scrape(getCurrentUrl());
            setPage(page + 1);
        }
    }

    /**
     * Retrieves the URL associated with the current page.
     * If the page is the first page, returns the original URL.
     * Otherwise, returns the URL with the page query parameter appended.
     *
     * @return The URL for the current page.
     */
    protected String getCurrentUrl() {
        if (isOnFirstPage()) {
            return String.format(url, getPage());
        }
        return String.format(urlWithPageQuery, getPage());
    }

    /**
     * Returns true if the {@link BaseWebScraper} is on the first page.
     * @return true if the scraper is on the first page
     */
    protected boolean isOnFirstPage() {
        return page == 1;
    }

    protected int getMaxPage() {
        return maxPage;
    }

    protected void setMaxPage(int maxPage){
        this.maxPage = maxPage;
    }

    protected void setMaxPage(){}

    /**
     * Retrieves and returns the HTML document of a webpage using Jsoup.
     *
     * @param url   the url to scrape
     * @return      the {@link Document} HTML document of the webpage
     * @see         Document
     */
    protected Document getDocument(String url) {
        Document doc = null;

        for (int tries = 1; tries <= CONNECT_TRIES; tries++) {
            try {
                doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Mobile Safari/537.36")
                        .header("Accept-Language", "nb-NO,nb;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate, br, zstd")
                        .timeout(10000)
                        .get();
                break; // Break if successful
            } catch (IOException ie) {
                try {
                    // Sleep a bit
                    Thread.sleep(WAIT_BEFORE_RECONNECT_MILLIS);
                } catch (InterruptedException e) {
                    logger.log(Level.SEVERE, "Could not sleep thread...", e.getMessage());
                }
                logger.log(Level.WARNING, "Could not get document for " + url + ". Tries=" + tries, ie);
            }
        }

        // Only stop scan if url is main and not a "detail page"
        if (Objects.isNull(doc) && url.equalsIgnoreCase(getCurrentUrl())) {
            continueScan = false;
            logger.warning( "Stopping scan after " + CONNECT_TRIES + " tries for " + getClass().getSimpleName());
            return null;
        }

        return doc;
    }

    /**
     * Checks if the scanning process should continue.
     *
     * @return  true if the scanning process should continue, false otherwise
     */
    protected boolean continueScan() {
        return continueScan;
    }

    protected void setContinueScan(boolean continueScan) {
        this.continueScan = continueScan;
    }

    public int getPage() {
        return page;
    }

    /**
     * Change the page of the website
     *
     * @param newPage   is the new page to be set
     */
    protected void setPage(int newPage) {
        this.page = newPage;
    }

    /**
     * Retrieves the proper definition name based on the provided definition key.
     * Converts the definition key to lowercase and looks up the corresponding value in the definition map.
     * If no matching definition is found, returns null.
     *
     * @param definitionKey The key for the definition to retrieve.
     * @return              The proper definition name corresponding to the key, or null if not found.
     */
    protected String retrieveProperDefinitionName(String definitionKey) {
        definitionKey = definitionKey.toLowerCase();
        return definitionMap.getOrDefault(definitionKey, null);
    }

    /**
     * Retrieves the correct value based on the provided key and value strings.
     * If the key is "sektor" and the value is neither "offentlig" nor "privat", returns "Ikke oppgitt".
     * Otherwise, returns the original value.
     *
     * @param key   The key associated with the value.
     * @param value The value to be checked and possibly modified.
     * @return      The correct value based on the key and value.
     */
    protected String retrieveCorrectValueForKey(String key, String value) {
        if (key.equalsIgnoreCase("sektor")) {
            if (!value.equalsIgnoreCase("offentlig") && !value.equalsIgnoreCase("privat")) {
                return "Ikke oppgitt";
            }
        }
        return value;
    }

    /**
     * Scrapes data from a website.
     *
     * @param url url to scrape
     */
    private void scrape(String url) {
        logger.info("Scraping " + url);

        Document doc = getDocument(url);
        if (Objects.isNull(doc)) {
            logger.info("Returning because doc returned null from " + url);
            return;
        }

        Elements jobPostElements = extractJobPostElements(doc);
        if (jobPostElements.isEmpty()) {
            logger.warning("Got no job post elements from " + url);
            return;
        }

        List<JobPost> jobPosts = buildJobPosts(url, jobPostElements);

        if (jobPosts.isEmpty()) {
            logger.warning("JobPosts from buildJobPosts was empty...");
            return;
        }

        logJobPostStatistics(jobPostElements.size(), jobPosts.size());

        tryToSendJobPosts(jobPosts);
    }

    /**
     * Extracts job post elements from the given document using XPath.
     *
     * @param doc   The document containing the job post elements.
     * @return      The extracted job post elements.
     */
    private Elements extractJobPostElements(Document doc) {
        // XPath value represents the job posting "cards"
        return doc.selectXpath(XPath);
    }

    /**
     * Builds a list of valid job posts from the given job post elements.
     *
     * @param url               The URL of the webpage from which the job post elements were extracted.
     * @param jobPostElements   The job post elements to be processed.
     * @return                  A list of valid job posts.
     */
    private List<JobPost> buildJobPosts(String url, Elements jobPostElements) {
        return jobPostElements.stream()
                .map(jobPostElement -> buildJobPost(url, jobPostElement))
                .filter(Objects::nonNull)
                .filter(JobPost::isValid)
                .toList();
    }

    /**
     * Logs the statistics of the job posts processed.
     *
     * @param totalJobPosts The total number of job posts processed.
     * @param validJobPosts The number of valid job posts created.
     */
    private void logJobPostStatistics(int totalJobPosts, int validJobPosts) {
        logger.info("[" + getClass().getSimpleName() + "]" +
                " of " + totalJobPosts + " job posts " + validJobPosts +
                " were successfully created");
    }

    /**
     * Tries to send the given list of job posts to the REST API client.
     * If successful, marks the URLs of the job posts as scraped.
     *
     * @param jobPosts The list of job posts to be sent.
     */
    private void tryToSendJobPosts(List<JobPost> jobPosts) {
        try {
            if (apiClient.tryToPostJobs(jobPosts)) {
                markUrlsAsScraped(jobPosts);
            }
        } catch (RuntimeException e) {
            continueScan = false;
            logger.log(Level.SEVERE, "Connecting to rest api client failed", e.getMessage());
        }
    }

    /**
     * Marks the URLs of the given job posts as scraped in the database.
     *
     * @param jobPosts The list of job posts whose URLs are to be marked as scraped.
     */
    private void markUrlsAsScraped(List<JobPost> jobPosts) {
        jobPosts.forEach(jobPost -> Database.insertUrl(jobPost.url()));
    }

    /**
     * Builds a {@link JobPost} object from the provided URL and HTML element.
     * Extracts various attributes of the job post from the HTML element and associated document.
     *
     * @param url       The URL of the web page where the job post is located.
     * @param element   The HTML element containing the job post information.
     * @return          A JobPost object representing the job post, or null if extraction fails.
     * @see             JobPost
     */
    private JobPost buildJobPost(String url, Element element) {
        String jobPostUrl = extractUrlForJobPostFromElement(url, element);

        if (Objects.isNull(jobPostUrl)) {
            logger.severe("Job post url returned null from " + url);
            return null;
        }

        if (Database.exists(jobPostUrl)) {
            return null;
        }

        String imageUrl = extractImageUrlForJobPostFromElement(url, element);
        String title = extractTitleForJobPostFromElement(url, element);

        Document jobPostDoc = this.getDocument(jobPostUrl);

        if (Objects.isNull(jobPostDoc)) {
            logger.severe("Returning null because doc returned thus resulting in jobpost being null");
            return null;
        }

        String companyName = extractCompanyNameForJobPostFromDoc(jobPostDoc);
        String companyImageUrl = extractCompanyImageUrlForJobPostFromDoc(jobPostDoc);
        LocalDate deadline = extractDeadlineForJobPostFromDoc(jobPostDoc);
        String description = extractDescriptionForJobPostFromDoc(jobPostDoc);
        Set<String> tags = extractTagsForJobPostFromDoc(jobPostDoc);

        Map<String, Set<String>> definitionMap = extractDefinitionsMapForJobPostFromDoc(jobPostDoc);

        // TODO IMPROVE
        if (!definitionMap.containsKey("Sektor")) {
            definitionMap.put("Sektor", Set.of("Ikke oppgitt"));
        }

        return new JobPost.Builder(jobPostUrl, imageUrl, title)
                .setCompanyName(companyName)
                .setCompanyImageUrl(companyImageUrl)
                .setDescription(description)
                .setIsDeadLineValid(deadline != null)
                .setDeadline(deadline)
                .setTags(tags)
                .setDefinitionMap(definitionMap)
                .build();
    }

    private void setStartPageFromArgument() {
        String pageToStartAt = Argument.getValue(Argument.START_PAGE);
        if (StringUtils.isNotEmpty(pageToStartAt)) {
            int page;
            try {
                page = Integer.parseInt(pageToStartAt);
                this.setPage(page);
                logger.info("Scraper " + getClass().getName() + " will start at page " + getPage());
            } catch (NumberFormatException e) {
                logger.severe("Could not parse '" + pageToStartAt + "' to a number!");
                System.exit(0);
            }
        }
    }

    private boolean isScraperDisabled() {
        String disabledWebsites = Argument.getValue(Argument.DISABLED_SCRAPERS);
        if (Objects.isNull(disabledWebsites)) {
            return false;
        }

        String[] disabledWebsitesArray = StringUtils.removeWhitespace(disabledWebsites).split(",");

        return Arrays.stream(disabledWebsitesArray)
                .anyMatch(url_ -> url.equalsIgnoreCase(url_)
                        || url.equalsIgnoreCase(urlWithPageQuery));
    }

    /**
     * Extracts the URL for the job post from the given HTML element.
     *
     * @param url       The URL of the web page where the element is located.
     * @param element   The HTML element containing the job post information.
     * @return          The URL for the job post, or null if not found.
     */
    abstract String extractUrlForJobPostFromElement(String url, Element element);

    /**
     * Extracts the image URL for the job post from the given HTML element.
     *
     * @param url       The URL of the web page where the element is located.
     * @param element   The HTML element containing the job post information.
     * @return          The image URL for the job post, or null if not found.
     */
    abstract String extractImageUrlForJobPostFromElement(String url, Element element);

    /**
     * Extracts the title for the job post from the given HTML element.
     *
     * @param url       The URL of the web page where the element is located.
     * @param element   The HTML element containing the job post information.
     * @return          The title for the job post, or null if not found.
     */
    abstract String extractTitleForJobPostFromElement(String url, Element element);

    /**
     * Extracts the company name for the job post from the given HTML document.
     *
     * @param doc   The HTML document containing the job post information.
     * @return      The company name, or an empty string if not found.
     */
    abstract String extractCompanyNameForJobPostFromDoc(Document doc);

    /**
     * Extracts the company image URL for the job post from the given HTML document.
     *
     * @param doc   The HTML document containing the job post information.
     * @return      The company image URL, or null if not found.
     */
    abstract String extractCompanyImageUrlForJobPostFromDoc(Document doc);

    /**
     * Extracts the description for the job post from the given HTML document.
     *
     * @param doc   The HTML document containing the job post information.
     * @return      The description for the job post, or null if not found.
     */
    abstract String extractDescriptionForJobPostFromDoc(Document doc);

    /**
     * Extracts the deadline for the job post from the given HTML document.
     *
     * @param doc   The HTML document containing the job post information.
     * @return      The deadline for the job post, or null if not found.
     */
    abstract LocalDate extractDeadlineForJobPostFromDoc(Document doc);

    /**
     * Extracts the tags for the job post from the given HTML document.
     *
     * @param doc   The HTML document containing the job post information.
     * @return      A list of tags for the job post, or an empty list if not found.
     */
    abstract Set<String> extractTagsForJobPostFromDoc(Document doc);

    /**
     * Extracts the description map for the job post from the given HTML document.
     *
     * @param doc   The HTML document containing the job post information.
     * @return      A map representing the description for the job post, or an empty map if not found.
     */
    abstract Map<String, Set<String>> extractDefinitionsMapForJobPostFromDoc(Document doc);

}