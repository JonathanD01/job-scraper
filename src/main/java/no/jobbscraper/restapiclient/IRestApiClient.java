package no.jobbscraper.restapiclient;

import no.jobbscraper.jobpost.JobPost;

import java.util.List;

sealed interface IRestApiClient permits BaseRestApiClient {

    /**
     * Attempts to post a list of job posts to a REST API.
     *
     * @param jobPosts          The list of job posts to be posted.
     * @return {@code true}     if the job posts were successfully posted; {@code false} otherwise.
     */
    boolean tryToPostJobs(List<JobPost> jobPosts);

}
