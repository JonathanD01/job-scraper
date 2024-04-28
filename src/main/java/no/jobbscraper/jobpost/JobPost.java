package no.jobbscraper.jobpost;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.jobbscraper.utils.StringUtils;

import java.time.LocalDate;
import java.util.*;

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

        @JsonProperty("deadline_valid")
        boolean isDeadlineValid,

        @JsonProperty("deadline")
        LocalDate deadline,

        @JsonProperty("job_tags")
        Set<String> tags,

        @JsonProperty("job_definitions")
        Map<String, Set<String>> jobDefinitionMap) {

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotEmpty(url) && StringUtils.isNotEmpty(companyName)
                && StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(description);
    }

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
        private boolean isDeadlineValid;
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

        public Builder setIsDeadLineValid(boolean isDeadlineValid) {
            this.isDeadlineValid = isDeadlineValid;
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
                    title, description, isDeadlineValid, deadline, tags, definitionMap);
        }

    }
}
