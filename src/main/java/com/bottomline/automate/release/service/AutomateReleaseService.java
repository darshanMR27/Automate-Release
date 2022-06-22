package com.bottomline.automate.release.service;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.util.concurrent.Promise;
import com.bottomline.automate.release.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class to keep all the business logic to automate
 * release branching using JQL query
 *
 * @author darshan.ramaiah
 */
@Service
@Slf4j
public class AutomateReleaseService {

    private static JiraRestClient restClient;

    /**
     * Method is to automate release branching using JQL query
     *
     * @param projectName
     * @param fixVersion
     * @param issueType
     * @param status
     * @param username
     * @param secretKey
     * @return
     * @throws Exception
     */
    public List<String> automateRelease(String projectName, String fixVersion, String issueType,
                                        String status, String username, String secretKey) throws Exception {

        log.info("AutomateReleaseService: automateRelease: Entry");
        int maxPerQuery = 100;
        int startIndex = 0;
        List<String> issueList = new ArrayList<>();
        try {
            URI jiraServerUri = URI.create(Constants.JIRA_BASE_URL);
            AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            AuthenticationHandler auth = new BasicHttpAuthenticationHandler(username, secretKey);
            restClient = factory.create(jiraServerUri, auth);
            String jqlString = constructJql(projectName, fixVersion, issueType, status);
            log.info("JQL String = " + jqlString);
            SearchRestClient searchRestClient = restClient.getSearchClient();
            while (true) {
                Promise<SearchResult> searchResult = searchRestClient.searchJql(jqlString,
                        maxPerQuery, startIndex, null);
                SearchResult results = searchResult.claim();
                log.debug("Search Results = " + results.getIssues());

                if (null != results.getIssues()) {
                    for (Issue issue : results.getIssues()) {
                        issueList.add(issue.getKey());
                    }
                }
                if (startIndex >= results.getTotal()) {
                    break;
                }
                startIndex += maxPerQuery;
                log.debug("Fetching from Index: " + startIndex);
            }
        } catch (Exception ex) {
            String errorMessage = "Exception while automating the release checklist" + ex.getMessage();
            log.error(errorMessage);
            throw new Exception(errorMessage);
        } finally {
            if (null != restClient)
                restClient.close();
        }
        log.info("AutomateReleaseService: automateRelease: Exit");
        return issueList;
    }

    /**
     * Method to construct JQL query from the input params
     *
     * @param project
     * @param fixVersion
     * @param issueType
     * @param status
     * @return
     */
    private String constructJql(String project, String fixVersion, String issueType, String status) {
        String jqlString = "project in (" + project + ") AND fixVersion in (" +
                fixVersion + ") AND issuetype in (" + issueType + ") AND status in (" + status + ")";
        return jqlString;
    }
}
