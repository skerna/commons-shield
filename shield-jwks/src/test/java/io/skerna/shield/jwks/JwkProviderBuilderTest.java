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