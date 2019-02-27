package io.skerna.shield.jwks;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;

@ExtendWith(MockitoExtension.class)
public class GuavaCachedJwkProviderTest {

    private static final String KID = "KID";
    private GuavaCachedJwkProvider provider;

    @Mock
    private JwkProvider fallback;

    @Mock
    private Jwk jwk;


    @BeforeEach
    public void setUp() throws Exception {
        provider = new GuavaCachedJwkProvider(fallback);
    }

    @Test
    public void shouldFailToGetSingle() throws Exception {
        Mockito.when(fallback.get(ArgumentMatchers.anyString())).thenThrow(new SigningKeyNotFoundException("TEST!", null));

        Assertions.assertThrows(
                SigningKeyNotFoundException.class,
                () ->{
                    provider.get(KID);
                }
        );
    }

    @Test
    public void shouldUseFallbackWhenNotCached() throws Exception {
        Mockito.when(fallback.get(ArgumentMatchers.eq(KID))).thenReturn(jwk);
        assertThat(provider.get(KID), Matchers.equalTo(jwk));
        Mockito.verify(fallback).get(ArgumentMatchers.eq(KID));
    }

    @Test
    public void shouldUseCachedValue() throws Exception {
        Mockito.when(fallback.get(ArgumentMatchers.eq(KID))).thenReturn(jwk).thenThrow(new SigningKeyNotFoundException("TEST!", null));
        provider.get(KID);
        assertThat(provider.get(KID), Matchers.equalTo(jwk));
        Mockito.verify(fallback, Mockito.only()).get(KID);
    }

    @Test
    public void shouldGetBaseProvider() throws Exception {
        assertThat(provider.getBaseProvider(), Matchers.equalTo(fallback));
    }
}