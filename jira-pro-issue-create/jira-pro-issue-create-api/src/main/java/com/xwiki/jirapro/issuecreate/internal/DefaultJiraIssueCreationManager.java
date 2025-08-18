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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.jira.config.JIRAAuthenticator;
import org.xwiki.contrib.jira.config.JIRAConfiguration;
import org.xwiki.contrib.jira.config.JIRAServer;
import org.xwiki.contrib.jira.config.internal.BasicAuthJIRAAuthenticator;
import org.xwiki.text.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.XWikiRequest;

/**
 * Default Jira Issue creation manager.
 * 
 * @version $Id$
 */
@Component
@Singleton
public class DefaultJiraIssueCreationManager implements JiraIssueCreationManager
{

    private static final String LABEL = "label";

    private static final String VALUE = "value";

    private static final String HINT = "hint";

    private static final String NAME = "name";

    private static final String DESCRIPTION = "description";

    private static final String SUBTASK = "subtask";

    private static final String REPORTER = "reporter";

    private static final String AVATAR_URLS = "avatarUrls";

    private static final String ICON = "icon";

    private static final String URL = "url";

    private static final String ISSUE_TYPE = "issuetype";

    private static final String PROJECT = "project";

    private static final String ID = "id";

    private static final String KEY = "key";

    private static final String VALUES = "values";

    @Inject
    private JIRAConfiguration jiraConfiguration;

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> xcontextProvider;

    /**
     * {@inheritDoc}
     * 
     * @see JiraIssueCreationManager#suggestInstance()
     */
    public String suggestInstance()
    {
        List<Map<String, String>> res = new ArrayList<>();

        for (Map.Entry<String, JIRAServer> entry : jiraConfiguration.getJIRAServers().entrySet()) {
            Map<String, String> resEntry = new TreeMap<>();
            resEntry.put(LABEL, entry.getKey());
            resEntry.put(VALUE, entry.getKey());
            resEntry.put(HINT, entry.getValue().getURL());
            res.add(resEntry);
        }

        return asJSON(res);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @see com.xwiki.bmc.macros.jira.internal.JiraIssueCreationManager#getAuthenticatorID(java.lang.String)
     */
    public String getAuthenticatorID(String instanceId)
    {
        JIRAServer jiraServer = jiraConfiguration.getJIRAServers().get(instanceId);
        Optional<JIRAAuthenticator> optionalAuthenticator = jiraServer.getJiraAuthenticator();

        if (optionalAuthenticator.isPresent()) {
            JIRAAuthenticator authenticator = optionalAuthenticator.get();
            return authenticator.getClass().getName();
        }

        return "";
    }

    /**
     * {@inheritDoc}
     * 
     * @see JiraIssueCreationManager#suggestProject(java.lang.String,
     *      java.lang.String)
     */
    public String suggestProject(String instanceId, String text)
    {
        String lowercaseText = text.toLowerCase();

        List<Map<String, Object>> res = new ArrayList<>();

        JsonNode jsonTree = asTree(getJiraIssueCreationRestClient(instanceId).getProjects());
        if (!jsonTree.isArray()) {
            throw new JiraIssueCreationException("Expected a JSON array in Jira response.");
        }

        for (JsonNode projectJSON : jsonTree) {
            if (res.size() > 20) {
                break;
            }

            Map<String, Object> resEntry = new TreeMap<>();

            String name = projectJSON.get(NAME).asText();
            String key = projectJSON.get(KEY).asText();

            if (!(name.toLowerCase().contains(lowercaseText) || key.toLowerCase().contains(lowercaseText))) {
                continue;
            }

            String iconURL = null;
            JsonNode avatarUrls = projectJSON.get(AVATAR_URLS);
            if (avatarUrls != null) {
                for (JsonNode iconJSON : avatarUrls) {
                    iconURL = iconJSON.asText();
                    break;
                }
            }

            resEntry.put(LABEL, name + ' ' + '(' + key + ')');
            resEntry.put(VALUE, key);
            if (iconURL != null) {
                resEntry.put(ICON, Collections.singletonMap(URL, iconURL));
            }

            res.add(resEntry);
        }

        return asJSON(res);
    }

    /**
     * {@inheritDoc}
     * 
     * @see JiraIssueCreationManager#suggestIssueType(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String suggestIssueType(String instanceId, String project, String text)
    {

        JsonNode jsonTree = asTree(getJiraIssueCreationRestClient(instanceId).getIssueTypes(project));

        JsonNode valuesTree = jsonTree.get(VALUES);
        if (valuesTree == null) {
            throw new JiraIssueCreationException("Invalid issuetypes JSON: could not find the 'values' attribute.");
        }

        if (!valuesTree.isArray()) {
            throw new JiraIssueCreationException("Invalid issuetypes JSON: 'values' attribute is not an array.");
        }

        List<Map<String, Object>> res = new ArrayList<>();

        for (JsonNode issueTypeJSON : valuesTree) {
            Map<String, Object> resEntry = new TreeMap<>();

            String name = issueTypeJSON.get(NAME).asText();
            String description = issueTypeJSON.get(DESCRIPTION).asText();
            Integer id = issueTypeJSON.get(ID).asInt();
            Boolean subtask = issueTypeJSON.get(SUBTASK).asBoolean();

            if (subtask) {
                continue;
            }

            String iconURL = issueTypeJSON.get("iconUrl").asText();

            resEntry.put(LABEL, name);

            if (description != null) {
                resEntry.put(HINT, description);
            }

            resEntry.put(VALUE, id);
            if (iconURL != null) {
                resEntry.put(ICON, Collections.singletonMap(URL, iconURL));
            }

            res.add(resEntry);
        }

        return asJSON(res);
    }

    /**
     * {@inheritDoc}
     * 
     * @see JiraIssueCreationManager#suggestAssignableUser(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String suggestAssignableUser(String instanceId, String project, String text)
    {
        JsonNode jsonTree = asTree(getJiraIssueCreationRestClient(instanceId).getAssignableUsers(project, text));
        return suggestUser(jsonTree);
    }

    /**
     * {@inheritDoc}
     * 
     * @see JiraIssueCreationManager#suggestUser(java.lang.String, java.lang.String)
     */
    public String suggestUser(String instanceId, String text)
    {
        JsonNode jsonTree = asTree(getJiraIssueCreationRestClient(instanceId).getUsers(text));
        return suggestUser(jsonTree);
    }

    /**
     * Suggest JIRA Users to pick from.
     * 
     * @param jsonTree the parsed JIRA Users JSON
     * @return The JSON selectize input for an Assignable Users suggest
     */
    private String suggestUser(JsonNode jsonTree)
    {
        List<Map<String, Object>> res = new ArrayList<>();

        for (JsonNode usersJSON : jsonTree) {
            if (res.size() > 20) {
                break;
            }

            Map<String, Object> resEntry = new TreeMap<>();

            String name = usersJSON.get(NAME).asText();
            String displayName = usersJSON.get("displayName").asText();
            String emailAddress = usersJSON.get("emailAddress").asText();

            String iconURL = null;
            JsonNode avatarUrls = usersJSON.get(AVATAR_URLS);
            if (avatarUrls != null) {
                for (JsonNode iconJSON : avatarUrls) {
                    iconURL = iconJSON.asText();
                    break;
                }
            }

            String formattedName = displayName + " - " + emailAddress + ' ' + '(' + name + ')';

            resEntry.put(LABEL, formattedName);
            resEntry.put(VALUE, name);
            if (iconURL != null) {
                resEntry.put(ICON, Collections.singletonMap(URL, iconURL));
            }

            res.add(resEntry);
        }

        return asJSON(res);
    }

    /**
     * {@inheritDoc}
     * 
     * @see JiraIssueCreationManager#getFieldsMetadata(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String getFieldsMetadata(String instanceId, String project, String issueType)
    {
        return getJiraIssueCreationRestClient(instanceId).getFieldsMetadata(project, issueType);
    }

    /**
     * {@inheritDoc}
     * 
     * @see JiraIssueCreationManager#createIssue(java.lang.String, java.lang.String)
     */
    public String createIssue(String instanceId, String inputData)
    {

        String inputJsonText = inputData;

        if (StringUtils.isEmpty(inputData)) {
            XWikiContext xcontext = xcontextProvider.get();
            XWikiRequest request = xcontext.getRequest();
            InputStream inputStream;
            try {
                inputStream = request.getInputStream();
            } catch (IOException e) {
                throw new JiraIssueCreationException("Could not retrieve POST data.", e);
            }
            BufferedReader bufferReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            bufferReader.lines().forEach(line -> {
                stringBuilder.append(line);
            });

            inputJsonText = stringBuilder.toString();
        }

        JsonNode inputTree = asTree(inputJsonText);

        JsonNode fieldsTree = inputTree.get("fields");
        if (fieldsTree == null) {
            throw new JiraIssueCreationException("Invalid Input Data.");
        }

        JsonNode issueTypeTree = fieldsTree.get(ISSUE_TYPE);
        JsonNode projectTree = fieldsTree.get(PROJECT);
        JsonNode reporterTree = fieldsTree.get(REPORTER);

        if (issueTypeTree == null) {
            throw new JiraIssueCreationException("Missing issuetype field.");
        }

        if (projectTree == null) {
            throw new JiraIssueCreationException("Missing project field.");
        }

        if (reporterTree != null) {
            throw new JiraIssueCreationException("Unexpected reporter field.");
        }

        JsonNode issueTypeIdTree = issueTypeTree.get(ID);
        JsonNode projectKeyTree = projectTree.get(KEY);

        if (issueTypeIdTree == null || !issueTypeIdTree.isTextual()) {
            throw new JiraIssueCreationException("Invalid issueType field.");
        }

        if (projectKeyTree == null || !projectKeyTree.isTextual()) {
            throw new JiraIssueCreationException("Invalid project field.");
        }

        String issueType = issueTypeIdTree.asText();
        String project = projectKeyTree.asText();

        ObjectNode fields = (ObjectNode) fieldsTree;
        if (isReporterNeeded(instanceId, project, issueType)) {
            JsonNode reporterField = asTree(asJSON(Collections.singletonMap(NAME, getReporterUserName(instanceId))));
            fields.set(REPORTER, reporterField);
        }

        return getJiraIssueCreationRestClient(instanceId).postIssue(asJSON(inputTree));
    }

    private boolean isReporterNeeded(String instanceId, String project, String issueType)
    {
        // Do not include a reporter field when we don't know the reporter username.
        if (getReporterUserName(instanceId) == null) {
            return false;
        }

        JsonNode fieldsMetadataTree = asTree(getFieldsMetadata(instanceId, project, issueType));
        if (fieldsMetadataTree == null) {
            throw new JiraIssueCreationException("Invalid fieldsMetadata JSON.");
        }

        JsonNode valuesTree = fieldsMetadataTree.get(VALUES);
        if (valuesTree == null) {
            throw new JiraIssueCreationException("Invalid fieldsMetadata JSON: could not find the 'values' attribute.");
        }

        if (!valuesTree.isArray()) {
            throw new JiraIssueCreationException("Invalid fieldsMetadata JSON: 'values' attribute is not an array.");
        }

        JsonNode[] reporterFieldTree = {null};

        // Look for the reporter field.
        valuesTree.forEach((JsonNode field) -> {
            JsonNode fieldIdTree = field.get("fieldId");
            if (fieldIdTree == null || !fieldIdTree.isTextual()) {
                return;
            }

            if (fieldIdTree.asText().equals(REPORTER)) {
                reporterFieldTree[0] = field;
            }
        });

        return reporterFieldTree[0] != null;
    }

    /**
     * Gets the reporter username.
     * 
     * @param instanceId the instance for which the username should be checked against
     * @return the username or null if the reporter field shouldn't be set.
     */
    private String getReporterUserName(String instanceId)
    {
        JIRAServer jiraServer = jiraConfiguration.getJIRAServers().get(instanceId);
        Optional<JIRAAuthenticator> authenticator = jiraServer.getJiraAuthenticator();
        if (authenticator.isPresent() && authenticator.get() instanceof BasicAuthJIRAAuthenticator) {
            String userName = xcontextProvider.get().getUserReference().getName();

            String jiraResponse = getJiraIssueCreationRestClient(instanceId).getUser(userName);

            JsonNode jiraUser = asTree(jiraResponse);

            if (jiraUser.has(NAME)) {
                return jiraUser.get(NAME).asText();
            }
        }

        return null;
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

    private JiraIssueCreationRestClient getJiraIssueCreationRestClient(String instanceId)
    {
        return new JiraIssueCreationRestClient(jiraConfiguration.getJIRAServers().get(instanceId));
    }
}
