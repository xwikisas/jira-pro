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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.apache.hc.client5.http.ContextBuilder;
import org.apache.hc.client5.http.classic.methods.HttpUriRequest;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.jira.config.JIRAAuthenticator;
import org.xwiki.contrib.oidc.OAuth2ClientManager;
import org.xwiki.contrib.oidc.OAuth2Token;
import org.xwiki.contrib.oidc.OAuth2TokenStore;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfiguration;
import org.xwiki.contrib.oidc.auth.store.OIDCClientConfigurationStore;
import org.xwiki.job.Job;
import org.xwiki.localization.ContextualLocalizationManager;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.FormatBlock;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.LinkBlock;
import org.xwiki.rendering.block.SpaceBlock;
import org.xwiki.rendering.block.WordBlock;
import org.xwiki.rendering.listener.Format;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;

import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.xpn.xwiki.XWikiContext;
import com.xwiki.licensing.Licensor;

/**
 * JIRA Rest Client provider with OAuth configuration.
 *
 * @version $Id$
 * @since 1.0.0
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Named(JIRAOAuthAuthenticator.HINT)
public class JIRAOAuthAuthenticator implements JIRAAuthenticator
{
    /**
     * The authenticator HINT.
     */
    public static final String HINT = "oauth";

    private static final String BLOCK_PARAM_CLASS = "class";

    private static final String BLOCK_PARAM_CLASS_VALUE_WARNINGMESSAGE = "box warningmessage";

    private static final String QUERY_STRING = "queryString";

    private String configurationName;

    private boolean isRequiringAuthentication;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Inject
    private OIDCClientConfigurationStore clientConfigurationStore;

    @Inject
    private OAuth2ClientManager oAuth2ClientManager;

    @Inject
    private OAuth2TokenStore tokenStore;

    @Inject
    private Logger logger;

    @Inject
    private ContextualLocalizationManager localization;

    @Inject
    private Licensor licensor;

    /**
     * Configure the authenticator with the parameter set into the configuration.
     *
     * @param configurationName see {@link #getConfigurationName()}
     * @param isRequiringAuthentication See {@link #isRequiringAuthentication()}
     */
    public void configure(String configurationName, boolean isRequiringAuthentication)
    {
        this.configurationName = configurationName;
        this.isRequiringAuthentication = isRequiringAuthentication;
    }

    @Override
    public AuthenticationHandler getRestClientAuthenticationHandler()
    {
        throw new RuntimeException("Feature not supported to configure a AuthenticationHandler for OAuth");
    }

    @Override
    public void authenticateInHttpClient(ContextBuilder context, HttpUriRequest request,
        HttpHost targetHost)
    {
        Optional<String> token = getOAuthToken();
        token.ifPresent(s -> request.setHeader("Authorization", "Bearer " + s));
    }

    @Override
    public boolean isAuthenticatingRequest()
    {
        return getOAuthToken().isPresent();
    }

    @Override
    public String getId()
    {
        return configurationName;
    }

    /**
     * @return provide the OAuth token related to the specific JIRA server for the specific user.
     */
    public Optional<String> getOAuthToken()
    {
        if (!licensor.hasLicensure(
            new DocumentReference(contextProvider.get().getWikiId(), List.of("XWiki", "JIRAPro", "OAuth"), "WebHome")))
        {
            return Optional.empty();
        }
        try {
            // Make sure that the token is up to date
            OIDCClientConfiguration configuration =
                clientConfigurationStore.getOIDCClientConfiguration(configurationName);
            Job tokenRenewalJob = oAuth2ClientManager.renew(configuration);
            if (tokenRenewalJob != null) {
                try {
                    tokenRenewalJob.join();
                } catch (InterruptedException e) {
                    logger.error("Can't get renewal job", e);
                    return Optional.empty();
                }
            }

            OAuth2Token token = tokenStore.getToken(configuration);
            if (token != null) {
                return Optional.of(token.getAccessToken());
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Can't OAuth token", e);
            return Optional.empty();
        }
    }

    /**
     * @return the OIDC configuration name.
     */
    public String getConfigurationName()
    {
        return configurationName;
    }

    /**
     * @return true if the all request must be sent with authentication and no result is expected if no authentication
     *     is provided. And false generally in case of public JIRA instance where the authentication might be not
     *     mandatory but just to increase the user experience.
     */
    public boolean isRequiringAuthentication()
    {
        return isRequiringAuthentication;
    }

    /**
     * Provide a block to show to the user if the user is not already authenticated.
     *
     * @param mustBeAuthenticated must be true, if in the UI the user must be authenticated, otherwise false when
     *     the feature are still usable in limited mode.
     * @param isInline if the content should be placed inline.
     * @param redirectUrl the URL to redirect after the authentication was done. Generally it should be something
     *     like {@code doc.URL} or {@code context.getURL().toString()}.
     * @return if the user need to be authenticated the warning block to show to user to proceed of the authentication,
     *     otherwise an empty block.
     */
    public Block getWarningMacroBlock(boolean mustBeAuthenticated, boolean isInline, String redirectUrl)
    {
        if (isAuthenticatingRequest()) {
            return isInline ? new FormatBlock() : new GroupBlock();
        }
        String linkTranslationKey;
        String descriptionTranslationKey;
        boolean isUserLoggedIn = contextProvider.get().getUserReference() != null;
        if (mustBeAuthenticated) {
            descriptionTranslationKey =
                isUserLoggedIn ? "com.xwiki.jirapro.oauth.mustbeauthenticated.jira.description"
                    : "com.xwiki.jirapro.oauth.mustbeauthenticated.xwiki.description";
            linkTranslationKey =
                isUserLoggedIn ? "com.xwiki.jirapro.oauth.mustbeauthenticated.jira.link"
                    : "com.xwiki.jirapro.oauth.mustbeauthenticated.xwiki.link";
        } else {
            descriptionTranslationKey =
                isUserLoggedIn ? "com.xwiki.jirapro.oauth.mightneedtoauthenticate.jira.description"
                    : "com.xwiki.jirapro.oauth.mightneedtoauthenticate.xwiki.description";
            linkTranslationKey =
                isUserLoggedIn ? "com.xwiki.jirapro.oauth.mightneedtoauthenticate.jira.link"
                    : "com.xwiki.jirapro.oauth.mightneedtoauthenticate.xwiki.link";
        }
        String configId = getConfigurationName();
        ResourceReference reference = isUserLoggedIn
            ? new ResourceReference("XWiki.JIRAPro.OAuth.JiraAuthorize", ResourceType.DOCUMENT)
            : new ResourceReference("/xwiki/bin/login/XWiki/XWikiLogin?xredirect="
            + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8), ResourceType.PATH);
        if (isUserLoggedIn) {
            reference.setParameter(QUERY_STRING,
                "configId=" + URLEncoder.encode(configId, StandardCharsets.UTF_8)
                    + "&redirectUrl=" + URLEncoder.encode(redirectUrl,
                    StandardCharsets.UTF_8));
        }
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
