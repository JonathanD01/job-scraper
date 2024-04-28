package no.jobbscraper.restapiclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.jobbscraper.argument.Argument;
import no.jobbscraper.jobpost.JobPost;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract sealed class BaseRestApiClient implements IRestApiClient permits FakeRestApiClient, RestApiClient {

    private static BaseRestApiClient instance = null;
    protected static final Logger logger = Logger.getLogger(BaseRestApiClient.class.getName());
    protected static final HttpClient httpClient = HttpClient.newHttpClient();
    protected static final ObjectMapper objectMapper = new ObjectMapper();
    private final String ip;
    private final String port;
    private final String path;
    private final String requestParam;

    protected BaseRestApiClient(String ip, String port, String path, String requestParam) {
        this.ip = ip;
        this.port = port;
        this.path = path;
        this.requestParam = requestParam;
    }

    public static BaseRestApiClient getInstance(boolean fake) {
        if (Objects.nonNull(instance)) {
            return instance;
        }

        if (fake) {
            instance = new FakeRestApiClient();
        } else {
            instance = new RestApiClient();
        }
        return instance;
    }

    protected BaseRestApiClient() {
        this.ip = Argument.getValue(Argument.IP);
        this.port = Argument.getValue(Argument.PORT);
        this.path = Argument.getValue(Argument.PATH);
        this.requestParam = Argument.getValue(Argument.REQUEST_PARAM);
        objectMapper.registerModule(new JavaTimeModule());
    }

    protected String transformJobPostsToString(List<JobPost> jobPosts)  {
        StringJoiner jsonString = new StringJoiner(",");
        for (JobPost jobPost : jobPosts) {
            try {
                jsonString.add(objectMapper.writeValueAsString(jobPost));
            } catch (JsonProcessingException e) {
                logger.log(Level.SEVERE, "Error occurred when transforming jobpost class to json " + jobPost.toString(), e.getMessage());
            }

        }
        return String.format("{\"%s\": [%s] }", requestParam, jsonString);
    }

    public String getPostUrl() {
        if (this.port != null) {
            return String.format("http://%s:%s/%s", this.ip, this.port, path);
        }
        return String.format("https://%s/%s", this.ip, path);
    }
}
