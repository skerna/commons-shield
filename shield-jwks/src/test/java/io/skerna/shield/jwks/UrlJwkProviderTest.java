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

package io.skerna.shield.jwks;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class UrlJwkProviderTest {

    private static final String KID = "NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg";

    @BeforeEach
    public void setUp() {
    }

    @Test
    public void shouldFailWithNullUrl() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new UrlJwkProvider((URL) null)
        );
    }

    @Test
    public void shouldFailToCreateWithNullDomain() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new UrlJwkProvider((String) null)
        );

    }

    @Test
    public void shouldFailToCreateWithEmptyDomain() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new UrlJwkProvider("")
        );
    }

    @Test
    public void shouldReturnSingleJwkById() throws Exception {
        UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/jwks.json"));
        assertThat(provider.get(KID), Matchers.notNullValue());
    }

    @Test
    public void shouldFailToLoadSingleWhenUrlHasNothing() throws Exception {
        Assertions.assertThrows(
                SigningKeyNotFoundException.class,
                () ->{
                    UrlJwkProvider provider = new UrlJwkProvider(new URL("file:///not_found.file"));
                    provider.get(KID);
                }
        );
    }

    @Test
    public void shouldFailToLoadSingleWhenKeysIsEmpty() throws Exception {
        Assertions.assertThrows(
                SigningKeyNotFoundException.class,
                () ->{
                    UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/empty-jwks.json"));
                    provider.get(KID);
                }
        );

    }


    @Test
    public void shouldFailToLoadSingleWhenJsonIsInvalid() throws Exception {
        Assertions.assertThrows(
                SigningKeyNotFoundException.class,
                () ->{
                    UrlJwkProvider provider = new UrlJwkProvider(getClass().getResource("/invalid-jwks.json"));
                    provider.get(KID);
                }
        );
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomain() {
        String domain = "samples.auth0.com";
        String actualJwksUrl = new UrlJwkProvider(domain).url.toString();
        MatcherAssert.assertThat(actualJwksUrl, Matchers.equalTo("https://" + domain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldWorkOnDomainWithSlash() {
        String domain = "samples.auth0.com";
        String domainWithSlash = domain + "/";
        String actualJwksUrl = new UrlJwkProvider(domainWithSlash).url.toString();
        MatcherAssert.assertThat(actualJwksUrl, Matchers.equalTo("https://" + domain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomainWithHttps() {
        String httpsDomain = "https://samples.auth0.com";
        String actualJwksUrl = new UrlJwkProvider(httpsDomain).url.toString();
        MatcherAssert.assertThat(actualJwksUrl, Matchers.equalTo(httpsDomain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpsUrlOnDomainWithHttpsAndSlash() {
        String httpsDomain = "https://samples.auth0.com";
        String httpsDomainWithSlash = httpsDomain + "/";
        String actualJwksUrl = new UrlJwkProvider(httpsDomainWithSlash).url.toString();
        MatcherAssert.assertThat(actualJwksUrl, Matchers.equalTo(httpsDomain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpUrlOnDomainWithHttp() {
        String httpDomain = "http://samples.auth0.com";
        String actualJwksUrl = new UrlJwkProvider(httpDomain).url.toString();
        MatcherAssert.assertThat(actualJwksUrl, Matchers.equalTo(httpDomain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldBuildCorrectHttpUrlOnDomainWithHttpAndSlash() {
        String httpDomain = "http://samples.auth0.com";
        String httpDomainWithSlash = httpDomain + "/";
        String actualJwksUrl = new UrlJwkProvider(httpDomainWithSlash).url.toString();
        MatcherAssert.assertThat(actualJwksUrl, Matchers.equalTo(httpDomain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldUseOnlyDomain() {
        String domain = "samples.auth0.com";
        String domainWithSubPath = domain + "/sub/path/";
        String actualJwksUrl = new UrlJwkProvider(domainWithSubPath).url.toString();
        MatcherAssert.assertThat(actualJwksUrl, Matchers.equalTo("https://" + domain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH));
    }

    @Test
    public void shouldFailOnInvalidProtocol() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    String domainWithInvalidProtocol = "httptest://samples.auth0.com";
                    new UrlJwkProvider(domainWithInvalidProtocol);
                }
        );

    }

    @Test
    public void shouldFailWithNegativeConnectTimeout() throws MalformedURLException {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    new UrlJwkProvider(new URL("https://localhost"), -1, null);
                }
        );
    }

    @Test
    public void shouldFailWithNegativeReadTimeout() throws MalformedURLException {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    new UrlJwkProvider(new URL("https://localhost"), null, -1);
                }
        );
    }

    /**
     * private static class MockURLStreamHandlerFactory implements URLStreamHandlerFactory {
     * <p>
     * // The weak reference is just a safeguard against objects not being released
     * // for garbage collection
     * private final WeakReference<URLConnection> value;
     * <p>
     * public MockURLStreamHandlerFactory(URLConnection urlConnection) {
     * this.value = new WeakReference<URLConnection>(urlConnection);
     * }
     *
     * @Override public URLStreamHandler createURLStreamHandler(String protocol) {
     * return "mock".equals(protocol) ? new URLStreamHandler() {
     * protected URLConnection openConnection(URL url) throws IOException {
     * try {
     * return value.get();
     * } finally {
     * value.clear();
     * }
     * }
     * } : null;
     * }
     * }
     **/
    @Test
    public void shouldConfigureURLConnectionTimeouts() throws Exception {
        URLConnection urlConnection = Mockito.mock(URLConnection.class);

        Mockito.when(urlConnection.getInputStream()).thenReturn(getClass().getResourceAsStream("/jwks.json"));

        URLStreamHandler stubUrlHandler = new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                return urlConnection;
            }
        };

        int connectTimeout = 10000;
        int readTimeout = 15000;

        UrlJwkProvider urlJwkProvider = new UrlJwkProvider(new URL("http://localhost", "", 80, "", stubUrlHandler), connectTimeout, readTimeout);
        Jwk jwk = urlJwkProvider.get("NkJCQzIyQzRBMEU4NjhGNUU4MzU4RkY0M0ZDQzkwOUQ0Q0VGNUMwQg");
        Assertions.assertNotNull(jwk);
        System.out.println(jwk);
        ArgumentCaptor<Integer> connectTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(urlConnection).setConnectTimeout(connectTimeoutCaptor.capture());
        MatcherAssert.assertThat(connectTimeoutCaptor.getValue(), Matchers.is(connectTimeout));

        ArgumentCaptor<Integer> readTimeoutCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.verify(urlConnection).setReadTimeout(readTimeoutCaptor.capture());
        MatcherAssert.assertThat(readTimeoutCaptor.getValue(), Matchers.is(readTimeout));
    }
}