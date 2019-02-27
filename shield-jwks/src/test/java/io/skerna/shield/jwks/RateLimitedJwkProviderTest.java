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

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class RateLimitedJwkProviderTest {

    private static final String KID = "KID";
    private RateLimitedJwkProvider provider;

    @Mock
    private JwkProvider fallback;

    @Mock
    private Jwk jwk;

    @Mock
    private Bucket bucket;

    @BeforeEach
    public void setUp() throws Exception {
        provider = new RateLimitedJwkProvider(fallback, bucket);
    }


    @Test
    public void shouldGetWhenBucketHasTokensAvailable() throws Exception {
        Mockito.when(bucket.consume()).thenReturn(true);
        Mockito.when(fallback.get(ArgumentMatchers.eq(KID))).thenReturn(jwk);
        assertThat(provider.get(KID), Matchers.equalTo(jwk));
        Mockito.verify(fallback).get(ArgumentMatchers.eq(KID));
    }

    @Test
    public void shouldGetBaseProvider() throws Exception {
        assertThat(provider.getBaseProvider(), Matchers.equalTo(fallback));
    }

}