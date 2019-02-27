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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Jwk provider that caches previously obtained Jwk in memory using a Google Guava cache
 */
@SuppressWarnings("WeakerAccess")
public class GuavaCachedJwkProvider implements JwkProvider {

    private final Cache<String, Jwk> cache;
    private final JwkProvider provider;

    /**
     * Creates a new provider that will cache up to 5 jwks for at most 10 hours
     * @param provider fallback provider to use when jwk is not cached
     */
    public GuavaCachedJwkProvider(final JwkProvider provider) {
        this(provider, 5, 10, TimeUnit.HOURS);
    }

    /**
     * Creates a new cached provider specifying cache size and ttl
     * @param provider fallback provider to use when jwk is not cached
     * @param size number of jwt to cache
     * @param expiresIn amount of time a jwk will live in the cache
     * @param expiresUnit unit of the expiresIn parameter
     */
    public GuavaCachedJwkProvider(final JwkProvider provider, long size, long expiresIn, TimeUnit expiresUnit) {
        this.provider = provider;
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(size)
                .expireAfterWrite(expiresIn, expiresUnit)
                .build();
    }

    @Override
    public Jwk get(final String keyId) throws JwkException {
        try {
            return cache.get(keyId, new Callable<Jwk>() {
                @Override
                public Jwk call() throws Exception {
                    return provider.get(keyId);
                }
            });
        } catch (ExecutionException e) {
            throw new SigningKeyNotFoundException("Failed to get key with kid " + keyId, e);
        }
    }

    @VisibleForTesting
    JwkProvider getBaseProvider() {
        return provider;
    }
}
