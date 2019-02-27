/*
 * Copyright (c)  2019  SKERNA
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.skerna.shield.jwtresolver;

import com.github.scribejava.apis.openid.OpenIdJsonTokenExtractor;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.util.Objects;
import java.util.WeakHashMap;

public class AccessTokenResolver implements TokenResolver   {
    private WeakHashMap<Integer, TokenWraper> mapAccessTokens = new WeakHashMap<>();

    private OAuth20Service service;
    private String refreshToken;

    private AccessTokenResolver(OAuth20Service service, String refreshToken)
    {
        this.service = service;
        this.refreshToken = refreshToken;
    }

    public OAuth2AccessToken resolveAccessToken(){
        DefaultApi20 api = service.getApi();
        String  apiId = service.getApiKey();
        String apiSecret = service.getApiSecret();

        int idToken = getInternalId(refreshToken);
        try {
            if(!hasAccessTokenLive(idToken)){
                String endpoint = api.getAccessTokenEndpoint();
                OAuthRequest request = new OAuthRequest(Verb.POST,"http://192.168.1.33:8000/auth/realms/r2b/protocol/openid-connect/token");
                request.addBodyParameter("grant_type","refresh_token");
                request.addBodyParameter("refresh_token",refreshToken);
                request.addBodyParameter("client_id",apiId);
                request.addBodyParameter("client_secret",apiSecret);

                Response resonse = service.execute(request);
                if(resonse.isSuccessful()){
                    OpenIdJsonTokenExtractor extractor  =  OpenIdJsonTokenExtractor.instance();
                    OAuth2AccessToken token = extractor.extract(resonse);
                    TokenWraper tokenWraper = new TokenWraper(token);
                    mapAccessTokens.put(idToken,tokenWraper);
                }else{
                    throw new TokenResolverException("respose from server ended with error "+resonse.toString());
                }
            }

            TokenWraper tokenw = mapAccessTokens.get(idToken);
            if(tokenw == null){
                throw new TokenResolverException("imposible resolve token, from cache or oauth server");
            }
            return tokenw.getToken();

        }catch (Exception ex){
            throw new TokenResolverException("No se pudo resolver el token",ex);
        }

    }
    boolean hasAccessTokenLive(String refreshToken) {
        int id = getInternalId(refreshToken);
        return hasAccessTokenLive(id);
    }

    /**
     * Check access token
     * @return
     */
    boolean hasAccessTokenLive(int idToken){
        TokenWraper token = mapAccessTokens.get(idToken);
        if(token == null){
            return false;
        }
        return System.currentTimeMillis() <  token.getSystemExpireTime();
    }

    public int getSize(){
        return mapAccessTokens.size();
    }
    private class TokenWraper  {
        OAuth2AccessToken token;
        Long fixedTime;

        public TokenWraper(OAuth2AccessToken token) {
            Objects.requireNonNull(token,"token cannot be null");
            this.token = token;
            this.fixedTime =  System.currentTimeMillis() + (token.getExpiresIn().longValue());
        }

        public OAuth2AccessToken getToken() {
            return token;
        }

        public Long getSystemExpireTime() {
            return fixedTime;
        }

        public int getId(){
            return getInternalId(token.getRefreshToken());
        }
    }
    private static int getInternalId(String string) {
        return string != null ? string.hashCode() * 31 : 0;  // PRIME = 31 or another prime number.
    }

    /**
     * Create new refresh Token
     * @param service
     * @param refreshToken
     * @return
     */
    public static AccessTokenResolver instance(OAuth20Service service,
                                               String refreshToken){
        return new AccessTokenResolver(service, refreshToken);
    }
}
