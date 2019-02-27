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

/**
 * Jwk provider that limits the amount of Jwks to deliver in a given rate.
 */
@SuppressWarnings("WeakerAccess")
public class RateLimitedJwkProvider implements JwkProvider {

    private final JwkProvider provider;
    private final Bucket bucket;

    /**
     * Creates a new provider that will check the given Bucket if a jwks can be provided now.
     *
     * @param bucket   bucket to limit the amount of jwk requested in a given amount of time.
     * @param provider provider to use to request jwk when the bucket allows it.
     */
    public RateLimitedJwkProvider(JwkProvider provider, Bucket bucket) {
        this.provider = provider;
        this.bucket = bucket;
    }

    @Override
    public Jwk get(final String keyId) throws JwkException {
        if (!bucket.consume()) {
            throw new RateLimitReachedException(bucket.willLeakIn());
        }
        return provider.get(keyId);
    }

    @VisibleForTesting
    JwkProvider getBaseProvider() {
        return provider;
    }
}
