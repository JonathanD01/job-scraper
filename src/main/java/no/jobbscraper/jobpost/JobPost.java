package no.jobbscraper.jobpost;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public record JobPost (
        @JsonProperty("url")
        String url,

        @JsonProperty("company_name")
        String companyName,

        @JsonProperty("company_image_url")
        String companyImageUrl,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("deadline")
        LocalDate deadline,

        @JsonProperty("job_tags")
        Set<String> tags,

        @JsonProperty("job_definitions")
        Map<String, Set<String>> jobDefinitionMap) {

    @Override
    public String toString() {
        int maxDescriptionLength = Math.min(20, description.length());
        return String.format("JobPost{url='%s', companyName='%s', companyImageUrl='%s', imageUrl='%s', title='%s', " +
                        "description='%s', deadline='%s', tags='%s', jobDefinitionMap='%s'}",
                url, companyName, companyImageUrl, imageUrl, title,
                description.substring(0, maxDescriptionLength) + "...", deadline, tags, jobDefinitionMap);
    }

    public static final class Builder {
        private final String url;
        private final String imageUrl;
        private final String title;
        private String companyName;
        private String companyImageUrl;
        private String description;
        private LocalDate deadline;
        private Set<String> tags;
        private Map<String, Set<String>> definitionMap;

        public Builder(String url, String imageUrl, String title) {
            this.url = url;
            this.imageUrl = imageUrl;
            this.title = title;
            this.tags = Collections.emptySet();
            this.definitionMap = Collections.emptyMap();
        }

        public Builder setCompanyName(String companyName) {
            this.companyName = companyName;
            return this;
        }

        public Builder setCompanyImageUrl(String companyImageUrl) {
            this.companyImageUrl = companyImageUrl;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setDeadline(LocalDate deadline) {
            this.deadline = deadline;
            return this;
        }

        public Builder setTags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public Builder setDefinitionMap(Map<String, Set<String>> definitionMap) {
            this.definitionMap = definitionMap;
            return this;
        }

        public JobPost build() {
            return new JobPost(url, companyName, companyImageUrl, imageUrl,
                    title, description, deadline, tags, definitionMap);
        }

    }
}
