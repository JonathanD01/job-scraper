package no.jobbscraper.restapiclient;

import no.jobbscraper.Main;
import no.jobbscraper.argument.Argument;
import no.jobbscraper.jobpost.JobPost;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;

public final class RestApiClient extends BaseRestApiClient {

    @Override
    public boolean tryToPostJobs(List<JobPost> jobPosts) {
        boolean disableRestClient = Objects.equals(Argument.getValue(Argument.DISABLE_REST_CLIENT), "yes");
        if (disableRestClient) {
            return false;
        }
        String data = transformJobPostsToString(jobPosts);
        HttpRequest request = getHttpRequest(data);
        return sendRequest(request, jobPosts.size());
    }

    private boolean sendRequest(HttpRequest request, int jobPostSize){
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                logger.info("Successfully posted " + jobPostSize + " job posts!");
                return true;
            }

            logger.severe("Posting " + jobPostSize + " returned status code "
                    + response.statusCode() + ", message: " + response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private HttpRequest getHttpRequest(String data){
        return HttpRequest.newBuilder()
                .uri(URI.create(getPostUrl()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(data))
                .build();
    }
}
