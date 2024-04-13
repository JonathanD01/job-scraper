package no.jobbscraper.url;

public enum WebsiteURL {

    FINN_NO("https://www.finn.no/job/fulltime/search.html"),
    FINN_NO_WITH_PAGE("https://www.finn.no/job/fulltime/search.html?page=%s"),
    KARRIERESTART_NO("https://karrierestart.no/jobb"),
    KARRIERESTART_NO_WITH_PAGE("https://karrierestart.no/jobb?ff=&page=%s"),
    ARBEIDSPLASSEN_NAV_NO("https://arbeidsplassen.nav.no/stillinger"),
    ARBEIDSPLASSEN_NAV_NO_WITH_PAGE("https://arbeidsplassen.nav.no/stillinger?from=%s"),
    ;

    private final String url;

    /**
     * Constructs a WebsiteURL enum with the specified WebsiteURL.
     *
     * @param url the WebsiteURL of the website
     */
    WebsiteURL(String url) {
        this.url = url;
    }

    /**
     * Returns the WebsiteURL of the website.
     *
     * @return the WebsiteURL of the website
     */
    public String get() {
        return url;
    }
}
