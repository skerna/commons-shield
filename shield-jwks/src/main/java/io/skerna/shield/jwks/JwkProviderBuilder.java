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

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * JwkProvider builder
 */
@SuppressWarnings("WeakerAccess")
public class JwkProviderBuilder {

    private final URL url;
    private TimeUnit expiresUnit;
    private long expiresIn;
    private long cacheSize;
    private boolean cached;
    private BucketImpl bucket;
    private boolean rateLimited;

    /**
     * Creates a new Builder with the given URL where to load the jwks from.
     *
     * @param url to load the jwks
     * @throws IllegalStateException if url is null
     */
    public JwkProviderBuilder(URL url) {
        if (url == null) {
            throw new IllegalStateException("Cannot build provider without url to jwks");
        }
        this.url = url;
        this.cached = true;
        this.expiresIn = 10;
        this.expiresUnit = TimeUnit.HOURS;
        this.cacheSize = 5;
        this.rateLimited = true;
        this.bucket = new BucketImpl(10, 1, TimeUnit.MINUTES);
    }

    /**
     * Creates a new Builder with a domain where to look for the jwks.
     * <br><br> It can be a url link 'https://samples.auth0.com' or just a domain 'samples.auth0.com'.
     * If the protocol (http or https) is not provided then https is used by default.
     * The default jwks path "/.well-known/jwks.json" is appended to the given string domain.
     * <br><br> For example, when the domain is "samples.auth0.com"
     * the jwks url that will be used is "https://samples.auth0.com/.well-known/jwks.json"
     * <br><br> Use {@link #JwkProviderBuilder(URL)} if you need to pass a full URL.
     * @param domain where jwks is published
     * @throws IllegalStateException if domain is null
     * @see UrlJwkProvider#UrlJwkProvider(String)
     */
    public JwkProviderBuilder(String domain) {
        this(buildJwkUrl(domain));
    }

    private static URL buildJwkUrl(String domain) {
        if (domain == null) {
            throw new IllegalStateException("Cannot build provider without domain");
        }
        return UrlJwkProvider.urlForDomain(domain);
    }

    /**
     * Toggle the cache of Jwk. By default the provider will use cache.
     *
     * @param cached if the provider should cache jwks
     * @return the builder
     */
    public JwkProviderBuilder cached(boolean cached) {
        this.cached = cached;
        return this;
    }

    /**
     * Enable the cache specifying size and expire time.
     *
     * @param cacheSize number of jwk to cache
     * @param expiresIn amount of time the jwk will be cached
     * @param unit      unit of time for the expire of jwk
     * @return the builder
     */
    public JwkProviderBuilder cached(long cacheSize, long expiresIn, TimeUnit unit) {
        this.cached = true;
        this.cacheSize = cacheSize;
        this.expiresIn = expiresIn;
        this.expiresUnit = unit;
        return this;
    }

    /**
     * Toggle the rate limit of Jwk. By default the Provider will use rate limit.
     *
     * @param rateLimited if the provider should rate limit jwks
     * @return the builder
     */
    public JwkProviderBuilder rateLimited(boolean rateLimited) {
        this.rateLimited = rateLimited;
        return this;
    }

    /**
     * Enable the cache specifying size and expire time.
     *
     * @param bucketSize max number of jwks to deliver in the given rate.
     * @param refillRate amount of time to wait before a jwk can the jwk will be cached
     * @param unit       unit of time for the expire of jwk
     * @return the builder
     */
    public JwkProviderBuilder rateLimited(long bucketSize, long refillRate, TimeUnit unit) {
        bucket = new BucketImpl(bucketSize, refillRate, unit);
        return this;
    }

    /**
     * Creates a {@link JwkProvider}
     *
     * @return a newly created {@link JwkProvider}
     */
    public JwkProvider build() {
        JwkProvider urlProvider = new UrlJwkProvider(url);
        if (this.rateLimited) {
            urlProvider = new RateLimitedJwkProvider(urlProvider, bucket);
        }
        if (this.cached) {
            urlProvider = new GuavaCachedJwkProvider(urlProvider, cacheSize, expiresIn, expiresUnit);
        }
        return urlProvider;
    }
}
