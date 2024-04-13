package no.jobbscraper.restapiclient;

import no.jobbscraper.jobpost.JobPost;

import java.util.List;

public final class FakeRestApiClient extends BaseRestApiClient {

    // TODO Implement test
    public FakeRestApiClient() {
        super("local", "local", "local", "local");
    }

    @Override
    public boolean tryToPostJobs(List<JobPost> jobPosts) {
        return false;
    }
}
