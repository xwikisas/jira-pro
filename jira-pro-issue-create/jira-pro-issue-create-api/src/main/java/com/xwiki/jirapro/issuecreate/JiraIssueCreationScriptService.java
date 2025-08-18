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
package com.xwiki.jirapro.issuecreate;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;

import com.xwiki.jirapro.issuecreate.internal.JiraIssueCreationManager;

/**
 * Helps XWiki scripts creating Jira issues.
 * 
 * @version $Id$
 */
@Component
@Named("jiraIssueCreation")
@Singleton
public class JiraIssueCreationScriptService implements ScriptService
{

    @Inject
    private JiraIssueCreationManager jiraIssueCreationManager;

    /**
     * Suggest JIRA instances to pick from.
     * 
     * @return The JSON selectize input for an instance suggest
     */
    public String suggestInstance()
    {
        return jiraIssueCreationManager.suggestInstance();
    }

    /**
     * Gets the ID of the Authenticator.
     *
     * @param instanceId the instance to get the Authenticator for.
     * @return The ID of the Authenticator if one is configured, an empty string otherwise.
     */
    public String getAuthenticatorID(String instanceId)
    {
        return jiraIssueCreationManager.getAuthenticatorID(instanceId);
    }

    /**
     * Suggest JIRA projects to pick from.
     * 
     * @param instanceId the instance the projects should be retrieved from
     * @param text string that must be contained in the project name or key.
     * @return The JSON selectize input for a project suggest
     */
    public String suggestProject(String instanceId, String text)
    {
        return jiraIssueCreationManager.suggestProject(instanceId, text);
    }

    /**
     * Suggest JIRA issue types to pick from.
     * 
     * @param instanceId the instance the issue types should be retrieved from
     * @param project the project from which the issue types should be picked
     * @param text string that must be contained in the issue type name or description.
     * @return The JSON selectize input for a issue type suggest
     */
    public String suggestIssueType(String instanceId, String project, String text)
    {
        return jiraIssueCreationManager.suggestIssueType(instanceId, project, text);
    }

    /**
     * Suggest JIRA Assignable Users to pick from.
     * 
     * @param instanceId the instance the Users should be retrieved from
     * @param project the project for which the users should be assignable
     * @param text search string
     * @return The JSON selectize input for an Assignable Users suggest
     */
    public String suggestAssignableUser(String instanceId, String project, String text)
    {
        return jiraIssueCreationManager.suggestAssignableUser(instanceId, project, text);
    }

    /**
     * Suggest JIRA Users to pick from.
     * 
     * @param instanceId the instance the Users should be retrieved from
     * @param text search string
     * @return The JSON selectize input for an Users suggest
     */
    public String suggestUser(String instanceId, String text)
    {
        return jiraIssueCreationManager.suggestUser(instanceId, text);
    }

    /**
     * Get the issue creation fields metadata.
     * 
     * @param instanceId
     * @param project
     * @param issueType
     * @return The JSON as returned by JIRA
     */
    public String getFieldsMetadata(String instanceId, String project, String issueType)
    {
        return jiraIssueCreationManager.getFieldsMetadata(instanceId, project, issueType);
    }

    /**
     * Create an issue.
     * 
     * @param inputData the issue fields as a JSON
     * @param instanceId the instance on which the issue should be created
     * @return The JSON as returned by JIRA
     */
    public String createIssue(String instanceId, String inputData)
    {

        return jiraIssueCreationManager.createIssue(instanceId, inputData);
    }

}
