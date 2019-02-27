package io.skerna.shield.jwtresolver;

import com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.util.function.Consumer;
import java.util.function.Function;

public interface TokenResolver {
    /**
     * Execute secure call with oidc server, internally renew tokens before  call apis
     * @return
     */
    OAuth2AccessToken resolveAccessToken();

}
