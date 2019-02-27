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