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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import org.xwiki.contrib.jira.config.JIRAServer;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.transformation.MacroTransformationContext;

import com.xpn.xwiki.XWikiContext;
import com.xwiki.jirapro.oauth.internal.JIRAOAuthAuthenticator;
import com.xwiki.licensing.Licensor;

/**
 * Macro transformation to add a message in case of the user is not logged in JIRA.
 *
 * @param <P> the type of the macro parameter.
 * @version $Id$
 * @since 1.0.0
 */
public class JIRAMacroTransformation<P> implements org.xwiki.contrib.jira.macro.JIRAMacroTransformation<P>
{
    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private Licensor licensor;

    @Override
    public List<Block> transform(List<Block> blocks, P parameters, MacroTransformationContext context,
        JIRAServer jiraServer, String macroName)
    {
        XWikiContext xwikiContext = contextProvider.get();
        if (jiraServer.getJiraAuthenticator().isEmpty()
            || !(jiraServer.getJiraAuthenticator().get() instanceof JIRAOAuthAuthenticator))
        {
            return blocks;
        }

        if (!licensor.hasLicensure(
            new DocumentReference(xwikiContext.getWikiId(), List.of("XWiki", "JIRAPro", "OAuth"), "WebHome")))
        {
            return List.of(new MacroBlock(
                "missingLicenseMessage",
                Map.of("extensionName", "com.xwiki.jirapro.oauth.extension.name"),
                null,
                context.isInline())
            );
        }

        JIRAOAuthAuthenticator authenticator = (JIRAOAuthAuthenticator) jiraServer.getJiraAuthenticator().get();
        if (authenticator.isAuthenticatingRequest()) {
            return blocks;
        }
        if (authenticator.isRequiringAuthentication()) {
            return List.of(authenticator.getWarningMacroBlock(true, context.isInline(),
                xwikiContext.getURL().toString()));
        } else {
            List<Block> result = new ArrayList<>(blocks);
            result.add(authenticator.getWarningMacroBlock(false, context.isInline(),
                xwikiContext.getURL().toString()));
            return result;
        }
    }
}
