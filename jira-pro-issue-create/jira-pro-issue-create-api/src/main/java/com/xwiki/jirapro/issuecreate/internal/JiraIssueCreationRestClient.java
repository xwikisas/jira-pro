/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.jirapro.issuecreate.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.xwiki.contrib.jira.config.JIRAServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Rest Client for JIRA Issue creation.
 * 
 * @version $Id$
 */
public class JiraIssueCreationRestClient
{

    private static final String APPLICATION_JSON = "application/json";

    private static final String CLIENT_CLOSE_FAILURE = "Failed to close client.";

    private static final String START_AT = "startAt";

    private static final String MAX_RESULTS = "maxResults";

    private static final String TOTAL = "total";

    private static final String IS_LAST = "isLast";

    private static final String VALUES = "values";

    private JIRAServer jiraServer;

    private URI hostURI;

    private HttpHost target;

    /**
     * Constructor.
     * 
     * @param jiraServer
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public JiraIssueCreationRestClient(JIRAServer jiraServer)
    {
        this.jiraServer = jiraServer;

        try {
            this.hostURI = new URI(jiraServer.getURL());
        } catch (URISyntaxException e) {
            throw new JiraIssueCreationException("Could not parse the Jira Server URL.", e);
        }
        target = new HttpHost(hostURI.getScheme(), hostURI.getHost(), hostURI.getPort());
    }

    /**
     * Get the local context for HTTP requests.
     * 
     * @return the local http context.
     */
    private HttpClientContext getLocalContext(HttpUriRequestBase request)
    {
        ContextBuilder builder = ContextBuilder.create();
        jiraServer.getJiraAuthenticator().ifPresent(a -> a.authenticateInHttpClient(builder, request, target));
        return builder.build();
    }

    /**
     * Get the HTTP Client for the JIRA Server with the correct auth setup.
     * 
     * @return the HTTP Client
     * @throws MalformedURLException
     */
    private CloseableHttpClient getHttpClient()
    {
        return HttpClients.createDefault();
    }

    /**
     * Get all projects.
     * 
     * @return The projects JSON returned by JIRA
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getProjects()
    {
        URI projectURI;
        projectURI = hostURI.resolve("/rest/api/2/project");
        return get(projectURI);
    }

    /**
     * Get all issue types for a given project.
     * 
     * @param project The project from which issue types should be retrieved
     * @return The issue types JSON returned by JIRA
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getIssueTypes(String project)
    {
        URI issueTypesURI;
        issueTypesURI = hostURI.resolve("/rest/api/2/issue/createmeta/" + urlEncode(project) + "/issuetypes");
        return paginatedGet(issueTypesURI);
    }

    /**
     * Get metadata for issue types used for creating issues.
     * 
     * @param project
     * @param issueType
     * @return The FieldsMetadata JSON returned by JIRA
     */
    public String getFieldsMetadata(String project, String issueType)
    {
        URI fieldsMetadataURI;
        fieldsMetadataURI = hostURI
            .resolve("/rest/api/2/issue/createmeta/" + urlEncode(project) + "/issuetypes/" + urlEncode(issueType));

        return paginatedGet(fieldsMetadataURI);
    }

    /**
     * Get assignable users for a project.
     * 
     * @param project
     * @param text The search query
     * @return The assignable users JSON returned by JRIA
     */
    public String getAssignableUsers(String project, String text)
    {
        URI assignableUsersURI;
        assignableUsersURI = hostURI.resolve(
            "/rest/api/2/user/assignable/search?project=" + urlEncode(project) + "&username=" + urlEncode(text));

        return get(assignableUsersURI);
    }

    /**
     * Get users.
     * 
     * @param text The search query
     * @return The assignable users JSON returned by JRIA
     */
    public String getUsers(String text)
    {
        URI usersURI;
        usersURI = hostURI.resolve("/rest/api/2/user/search?username=" + urlEncode(text));

        return get(usersURI);
    }

    /**
     * Get user.
     * 
     * @param text The search query
     * @return The assignable users JSON returned by JIRA
     */
    public String getUser(String text)
    {
        URI userURI;
        userURI = hostURI.resolve("/rest/api/2/user?username=" + urlEncode(text));

        return get(userURI);
    }

    /**
     * Post issue.
     * 
     * @param inputData
     * @return the isse creation JSON returned by JIRA
     */
    public String postIssue(String inputData)
    {
        URI createIssueURI;
        createIssueURI = hostURI.resolve("/rest/api/2/issue");

        return post(createIssueURI, inputData);
    }

    /**
     * Combines result from all pages into a single JSON Array.
     * 
     * @param uri the URI to GET without pagination parameters
     * @return the concatenated received arrays.
     */
    public String paginatedGet(URI uri)
    {
        int total = 0;
        boolean isLast = false;

        List<JsonNode> entries = new ArrayList<>();

        while (!isLast) {
            URI paginationURI = formatPaginationParameters(entries.size(), uri);
            String rawResponse = get(paginationURI);
            JsonNode response = asTree(rawResponse);
            if (response.has(TOTAL)) {
                total = response.get(TOTAL).asInt();
            }
            if (response.has(IS_LAST)) {
                isLast = response.get(IS_LAST).asBoolean();
            } else {
                throw new JiraIssueCreationException("Expected a paginated response.");
            }

            if (response.has(VALUES)) {
                response.get(VALUES).forEach(entry -> {
                    entries.add(entry);
                });
            } else {
                throw new JiraIssueCreationException("Expected a paginated response.");
            }
        }

        return asJSON(Map.of(START_AT, 0, MAX_RESULTS, entries.size(), TOTAL, total, IS_LAST, isLast, VALUES, entries));
    }

    private static URI formatPaginationParameters(int start, URI baseURI)
    {
        String baseURIString = baseURI.toString();
        String baseURIQueryString = baseURI.getQuery();
        boolean extraParam = baseURIQueryString != null && !baseURIQueryString.isBlank();
        try {
            return new URI(
                baseURIString + (extraParam ? '&' : '?') + START_AT + '=' + urlEncode(Integer.toString(start)));
        } catch (URISyntaxException e) {
            throw new JiraIssueCreationException("Failed to format pagination parameters.", e);
        }
    }

    /**
     * Performs a GET request with proper Auth to a Jira instance.
     * 
     * @param uri
     * @return the response
     */
    private String get(URI uri)
    {
        CloseableHttpClient client = getHttpClient();

        try {
            HttpGet httpget = new HttpGet(uri);
            httpget.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
            try {
                return client.execute(target, httpget, getLocalContext(httpget), response -> {
                    return EntityUtils.toString(response.getEntity());
                }).toString();
            } catch (IOException e) {
                throw new JiraIssueCreationException("Failed to perform a GET Request.", e);
            }
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                throw new JiraIssueCreationException(CLIENT_CLOSE_FAILURE, e);
            }
        }
    }

    /**
     * Performs a POST request with proper Auth to a Jira instance.
     * 
     * @param uri
     * @param data
     * @return the response
     */
    private String post(URI uri, String data)
    {
        CloseableHttpClient client = getHttpClient();

        try {
            HttpPost httppost = new HttpPost(uri);
            httppost.setEntity(new StringEntity(data));
            httppost.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
            httppost.setHeader(HttpHeaders.ACCEPT, APPLICATION_JSON);
            try {
                return client.execute(target, httppost, getLocalContext(httppost), response -> {
                    return EntityUtils.toString(response.getEntity());
                }).toString();
            } catch (IOException e) {
                throw new JiraIssueCreationException("Failed to perform a POST Request.", e);
            }
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                throw new JiraIssueCreationException(CLIENT_CLOSE_FAILURE, e);
            }
        }
    }

    /**
     * URL Encode the given string.
     * 
     * @param input
     * @return the URL encoded string
     */
    private static String urlEncode(String input)
    {
        return URLEncoder.encode(input, StandardCharsets.UTF_8);
    }

    private static String asJSON(Object object)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JiraIssueCreationException("Failed to serialize an object as JSON.", e);
        }
    }

    private static JsonNode asTree(String json)
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new JiraIssueCreationException("Failed to parse JSON.", e);
        }
    }

}
