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
package com.xwiki.jirapro.oauth.internal.transformation;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.contrib.jira.config.JIRAServer;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.jirapro.oauth.internal.JIRAOAuthAuthenticator;

/**
 * Macro transformation to add a message in case of the user is not logged in JIRA.
 *
 * @param <P> the type of the macro parameter.
 * @version $Id$
 * @since 1.0.0
 */
public class JIRAMacroTransformation<P> implements org.xwiki.contrib.jira.macro.JIRAMacroTransformation<P>
{
    private static final String BLOCK_PARAM_CLASS = "class";

    private static final String BLOCK_PARAM_CLASS_VALUE_WARNINGMESSAGE = "box warningmessage";

    @Inject
    private Logger logger;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private ContextualLocalizationManager localization;

    @Override
    public List<Block> transform(List<Block> blocks, P parameters,
        MacroTransformationContext context, JIRAServer jiraServer, String macroName)
    {
        if (jiraServer.getJiraAuthenticator().isEmpty() || !(jiraServer.getJiraAuthenticator()
            .get() instanceof JIRAOAuthAuthenticator))
        {
            return blocks;
        }
        JIRAOAuthAuthenticator authenticator = (JIRAOAuthAuthenticator) jiraServer.getJiraAuthenticator().get();
        Optional<String> token = authenticator.getOAuthToken();
        if (token.isPresent()) {
            return blocks;
        }
        try {
            if (authenticator.isRequiringAuthentication()) {
                return List.of(
                    getWarningMacroBlock(authenticator.getConfigurationName(),
                        "com.xwiki.jirapro.oauth.mustbeauthenticated.description",
                        "com.xwiki.jirapro.oauth.mustbeauthenticated.link",
                        context.isInline()));
            } else {
                List<Block> result = new ArrayList<>(blocks);
                result.add(
                    getWarningMacroBlock(authenticator.getConfigurationName(),
                        "com.xwiki.jirapro.oauth.mightneedtoauthenticate.description",
                        "com.xwiki.jirapro.oauth.mightneedtoauthenticate.link",
                        context.isInline()));
                return result;
            }
        } catch (ComponentLookupException | UnsupportedEncodingException e) {
            logger.error("Cant' get renderer component", e);
            return blocks;
        }
    }

    private Block getWarningMacroBlock(String configId, String descriptionTranslationKey,
        String linkTranslationKey, boolean isInline)
        throws ComponentLookupException, UnsupportedEncodingException
    {
        ResourceReference reference = new ResourceReference("XWiki.JIRAPro.OAuth.JiraAuthorize", ResourceType.DOCUMENT);
        reference.setParameter("queryString",
            "configId=" + URLEncoder.encode(configId, StandardCharsets.UTF_8)
                + "&redirectUrl=" + URLEncoder.encode(contextProvider.get().getURL().toString(),
                StandardCharsets.UTF_8));
        LinkBlock link = new LinkBlock(
            localization.getTranslation(linkTranslationKey).render().getChildren(),
            reference,
            false);
        List<Block> blocks = List.of(
            localization.getTranslation(descriptionTranslationKey).render(),
            new SpaceBlock(),
            link,
            new WordBlock("."));
        if (isInline) {
            return new FormatBlock(blocks, Format.NONE, Map.of(BLOCK_PARAM_CLASS,
                BLOCK_PARAM_CLASS_VALUE_WARNINGMESSAGE));
        } else {
            return new GroupBlock(blocks, Map.of(BLOCK_PARAM_CLASS, BLOCK_PARAM_CLASS_VALUE_WARNINGMESSAGE));
        }
    }
}
