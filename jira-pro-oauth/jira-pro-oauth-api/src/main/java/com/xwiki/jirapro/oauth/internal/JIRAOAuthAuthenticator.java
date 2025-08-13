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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

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

import com.atlassian.jira.rest.client.api.AuthenticationHandler;

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

    private String configurationName;

    private boolean isRequiringAuthentication;

    @Inject
    private OIDCClientConfigurationStore clientConfigurationStore;

    @Inject
    private OAuth2ClientManager oAuth2ClientManager;

    @Inject
    private OAuth2TokenStore tokenStore;

    @Inject
    private Logger logger;

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
     * @return true if the all request must be sent with authentication and no result is
     * expected if no authentication is provided. And false generally in case of public JIRA instance where the
     * authentication might be not mandatory but just to increase the user experience.
     */
    public boolean isRequiringAuthentication()
    {
        return isRequiringAuthentication;
    }
}
