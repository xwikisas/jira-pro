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
package com.xwiki.jirapro.oauth.internal;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.contrib.jira.config.JIRAAuthenticator;
import org.xwiki.contrib.jira.config.JIRAAuthenticatorFactory;
import org.xwiki.contrib.jira.config.internal.JIRAAuthenticatorException;
import org.xwiki.model.reference.LocalDocumentReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * The JIRA Authenticator factory for OAuth.
 *
 * @version $Id$
 * @since 1.0.0
 */
@Singleton
@Component
@Named(JIRAOAuthAuthenticator.HINT)
public class JIRAOAuthAuthenticatorFactory implements JIRAAuthenticatorFactory
{
    private static final List<String> JIRA_PRO_OAUTH_SPACE = List.of("XWiki", "JIRAPro", "OAuth");

    private static final String CONFIG_ID_FIELD = "id";

    /**
     * The document for storing the basic auth configuration.
     */
    public static final LocalDocumentReference OAUTH_CONFIG_REFERENCE =
        new LocalDocumentReference(JIRA_PRO_OAUTH_SPACE, "Config");

    /**
     * The class reference for the basic auth configuration.
     */
    public static final LocalDocumentReference OAUTH_DATA_CLASS_REFERENCE =
        new LocalDocumentReference(JIRA_PRO_OAUTH_SPACE, "ConfigClass");

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override
    public JIRAAuthenticator get(String serverId) throws JIRAAuthenticatorException
    {
        XWikiContext context = contextProvider.get();
        XWikiDocument doc;
        try {
            doc = context.getWiki()
                .getDocument(OAUTH_CONFIG_REFERENCE, context);
        } catch (XWikiException e) {
            throw new JIRAAuthenticatorException("Can't get JIRA OAuth configuration document", e);
        }
        Optional<BaseObject> authObj = doc.getXObjects(OAUTH_DATA_CLASS_REFERENCE)
            .stream().filter(x -> StringUtils.equals(serverId, (x.getStringValue(CONFIG_ID_FIELD))))
            .findFirst();
        if (authObj.isPresent()) {
            int requireAuthentication = authObj.get().getIntValue("requireAuthentication");
            String configName = authObj.get().getStringValue("oidcConfigName");
            JIRAOAuthAuthenticator authenticator;
            try {
                authenticator =
                    componentManagerProvider.get().getInstance(JIRAAuthenticator.class, JIRAOAuthAuthenticator.HINT);
            } catch (ComponentLookupException e) {
                throw new JIRAAuthenticatorException("Can't get OAuthAuthenticator component", e);
            }
            authenticator.configure(configName, requireAuthentication == 1);
            return authenticator;
        } else {
            throw new JIRAAuthenticatorException("Can't find OAuth config for server ID: " + serverId);
        }
    }
}
