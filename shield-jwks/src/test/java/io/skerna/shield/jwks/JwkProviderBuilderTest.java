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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class JwkProviderBuilderTest {

    private String domain = "samples.auth0.com";
    private String normalizedDomain = "https://" + domain;

    @Test
    public void shouldCreateForUrl() throws Exception {
        URL urlToJwks = new URL(normalizedDomain + UrlJwkProvider.WELL_KNOWN_JWKS_PATH);
        assertThat(new JwkProviderBuilder(urlToJwks).build(), Matchers.notNullValue());
    }

    @Test
    public void shouldCreateForDomain() {
        assertThat(new JwkProviderBuilder(domain).build(), Matchers.notNullValue());
    }

    @Test
    public void shouldCreateForNormalizedDomain() {
        assertThat(new JwkProviderBuilder(normalizedDomain).build(), Matchers.notNullValue());
    }

    @Test
    public void shouldFailWhenNoUrlIsProvided() {
        IllegalStateException thrown = Assertions.assertThrows(
                IllegalStateException.class,
                ()-> new JwkProviderBuilder((URL) null).build()
        );
        Assertions.assertEquals("Cannot build provider without url to jwks",thrown.getMessage());
    }

    @Test
    public void shouldFailWhenNoDomainIsProvided() {
        IllegalStateException thrown = Assertions.assertThrows(
                IllegalStateException.class,
                ()-> new JwkProviderBuilder((String) null).build()
        );
        Assertions.assertEquals("Cannot build provider without domain",thrown.getMessage());
    }

    @Test
    public void shouldCreateCachedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(true)
                .build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(GuavaCachedJwkProvider.class));
        assertThat(((GuavaCachedJwkProvider) provider).getBaseProvider(), Matchers.instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .rateLimited(false)
                .cached(10, 24, TimeUnit.HOURS)
                .build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(GuavaCachedJwkProvider.class));
        assertThat(((GuavaCachedJwkProvider) provider).getBaseProvider(), Matchers.instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateRateLimitedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(false)
                .rateLimited(true)
                .build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) provider).getBaseProvider(), Matchers.instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateRateLimitedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(false)
                .rateLimited(10, 24, TimeUnit.HOURS)
                .build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) provider).getBaseProvider(), Matchers.instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProvider() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(true)
                .rateLimited(true)
                .build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        MatcherAssert.assertThat(baseProvider, Matchers.instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), Matchers.instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderWithCustomValues() {
        JwkProvider provider = new JwkProviderBuilder(domain)
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 24, TimeUnit.HOURS)
                .build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        MatcherAssert.assertThat(baseProvider, Matchers.instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), Matchers.instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldCreateCachedAndRateLimitedProviderByDefault() {
        JwkProvider provider = new JwkProviderBuilder(domain).build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(GuavaCachedJwkProvider.class));
        JwkProvider baseProvider = ((GuavaCachedJwkProvider) provider).getBaseProvider();
        MatcherAssert.assertThat(baseProvider, Matchers.instanceOf(RateLimitedJwkProvider.class));
        assertThat(((RateLimitedJwkProvider) baseProvider).getBaseProvider(), Matchers.instanceOf(UrlJwkProvider.class));
    }

    @Test
    public void shouldSupportUrlToJwksDomainWithSubPath() throws Exception {
        String urlToJwksWithSubPath = normalizedDomain + "/sub/path" + UrlJwkProvider.WELL_KNOWN_JWKS_PATH;
        URL url = new URL(urlToJwksWithSubPath);
        JwkProvider provider = new JwkProviderBuilder(url)
                .rateLimited(false)
                .cached(false)
                .build();
        MatcherAssert.assertThat(provider, Matchers.notNullValue());
        MatcherAssert.assertThat(provider, Matchers.instanceOf(UrlJwkProvider.class));
        UrlJwkProvider urlJwkProvider = (UrlJwkProvider) provider;
        assertThat(urlJwkProvider.url.toString(), Matchers.equalTo(urlToJwksWithSubPath));
    }
}