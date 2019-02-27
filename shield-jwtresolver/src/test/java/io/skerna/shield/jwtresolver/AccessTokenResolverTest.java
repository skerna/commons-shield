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

import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class AccessTokenResolverTest {

    void resolveAccessToken() {
        String refresh = "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJjMjMxMzQ4OS00Y2QyLTRkNGQtYTA3MC05YmQ0ODRkOTRmMjYifQ.eyJqdGkiOiIwMjJiYmVjYi0zZTkxLTRhZWEtOTRmNC1iNjA2YzllYmI3MGQiLCJleHAiOjAsIm5iZiI6MCwiaWF0IjoxNTQwMTM5MzUxLCJpc3MiOiJodHRwOi8vMTkyLjE2OC4xLjMzOjgwMDAvYXV0aC9yZWFsbXMvcjJiIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjYxNzBlODdkLWQwYzQtNDRiMS1iODRkLTE5ZTk4MDlmYjUzMSIsInR5cCI6Ik9mZmxpbmUiLCJhenAiOiJhY2NvdW50IiwiYXV0aF90aW1lIjowLCJzZXNzaW9uX3N0YXRlIjoiZjllYjZiYWQtNTAyZS00MzA1LTg5N2QtMzFmYjcxMTlmZGI4IiwicmVhbG1fYWNjZXNzIjp7InJvbGVzIjpbIm9mZmxpbmVfYWNjZXNzIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJwcm9maWxlIG9mZmxpbmVfYWNjZXNzIGVtYWlsIn0.LeYpfV7RCYDNA8ih_ruMGVIkFvg17w-Vla6FgTJ-h64";
        OAuth20Service service = new ServiceBuilder("account")
                .apiSecret("0211d867-39dd-400b-bcca-03f109f8110f")
                .build(KeycloakApi.instance("http://192.168.1.33:8000", "r2b"));
        

        AccessTokenResolver accessTokenResolver = AccessTokenResolver.instance(service,refresh);

        for (int i = 0; i <1000 ; i++) {
            OAuth2AccessToken result = accessTokenResolver.resolveAccessToken();
            System.out.println(accessTokenResolver.getSize());
            System.out.println(accessTokenResolver.hasAccessTokenLive(refresh));

        }

    }
    @Test
    void tokenLive(){

    }

}
