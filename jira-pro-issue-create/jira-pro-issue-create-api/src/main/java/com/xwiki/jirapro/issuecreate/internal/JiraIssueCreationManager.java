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

import org.xwiki.component.annotation.Role;

/**
 * Handles the Jira Issue creation process.
 * 
 * @version $Id$
 */
@Role
public interface JiraIssueCreationManager
{

    /**
     * Suggest JIRA instances to pick from.
     * 
     * @return The JSON selectize input for an instance suggest
     */
    String suggestInstance();

    /**
     * Gets the ID of the Authenticator.
     *
     * @param instanceId the instance to get the Authenticator for.
     * @return The ID of the Authenticator if one is configured, an empty string otherwise.
     */
    String getAuthenticatorID(String instanceId);

    /**
     * Suggest JIRA projects to pick from.
     * 
     * @param instanceId the instance the projects should be retrieved from
     * @param text string that must be contained in the project name or key.
     * @return The JSON selectize input for a project suggest
     */
    String suggestProject(String instanceId, String text);

    /**
     * Suggest JIRA issue types to pick from.
     * 
     * @param instanceId the instance the issue types should be retrieved from
     * @param project the project from which the issue types should be picked
     * @param text string that must be contained in the issue type name or description.
     * @return The JSON selectize input for a issue type suggest
     */
    String suggestIssueType(String instanceId, String project, String text);

    /**
     * Suggest JIRA Assignable Users to pick from.
     * 
     * @param instanceId the instance the Users should be retrieved from
     * @param project the project for which the users should be assignable
     * @param text search string
     * @return The JSON selectize input for an Assignable Users suggest
     */
    String suggestAssignableUser(String instanceId, String project, String text);

    /**
     * Suggest JIRA Users to pick from.
     * 
     * @param instanceId the instance the Users should be retrieved from
     * @param text search string
     * @return The JSON selectize input for an Users suggest
     */
    String suggestUser(String instanceId, String text);

    /**
     * Get the issue creation fields metadata.
     * 
     * @param instanceId
     * @param project
     * @param issueType
     * @return The JSON as returned by JIRA
     */
    String getFieldsMetadata(String instanceId, String project, String issueType);

    /**
     * Create an issue.
     * 
     * @param instanceId the instance to create the issue on
     * @param inputData the issue fields as a JSON
     * @return The JSON as returned by JIRA
     */
    String createIssue(String instanceId, String inputData);
}
