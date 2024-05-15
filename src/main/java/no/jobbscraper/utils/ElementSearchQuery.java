package no.jobbscraper.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Collections;
import java.util.List;

public record ElementSearchQuery(
        Document document,
        String url,
        Element element,
        String cssQuery,
        String XPath,
        String attributeToReturn,
        boolean text,
        boolean ownText,
        boolean html,
        List<String> requiredAttributes) {

    public static class Builder {
        private Document document;
        private String url;
        private Element element;
        private String cssQuery;
        private String XPath;
        private String attributeToReturn;
        private boolean text;
        private boolean ownText;
        private boolean html;
        private List<String> requiredAttributes;

        public Builder(Document document) {
            this.document = document;
            this.text = false;
            this.ownText = false;
            this.html = false;
            this.requiredAttributes = Collections.emptyList();
        }

        public Builder(String url, Element element) {
            this.url = url;
            this.element = element;
            this.text = false;
            this.ownText = false;
            this.html = false;
            this.requiredAttributes = Collections.emptyList();
        }

        public Builder setCssQuery(String cssQuery) {
            this.cssQuery = cssQuery;
            return this;
        }

        public Builder setXPath(String XPath) {
            this.XPath = XPath;
            return this;
        }

        public Builder attributeToReturn(String attributeToReturn) {
            this.attributeToReturn = attributeToReturn;
            return this;
        }

        public Builder text() {
            this.text = true;
            return this;
        }

        public Builder ownText() {
            this.ownText = true;
            return this;
        }

        public Builder html() {
            this.html = true;
            return this;
        }

        public Builder setRequiredAttributes(List<String> requiredAttributes) {
            this.requiredAttributes = requiredAttributes;
            return this;
        }

        public ElementSearchQuery build(){
            return new ElementSearchQuery(document, url, element, cssQuery,
                    XPath, attributeToReturn, text, ownText, html, requiredAttributes);
        }
    }
}
