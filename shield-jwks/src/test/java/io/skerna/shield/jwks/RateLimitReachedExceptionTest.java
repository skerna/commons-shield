package io.skerna.shield.jwks;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class RateLimitReachedExceptionTest {
    public void shouldGetAvailableIn() throws Exception {
        RateLimitReachedException exception = new RateLimitReachedException(123456789);
        MatcherAssert.assertThat(exception, Matchers.notNullValue());
        assertThat(exception.getMessage(), Matchers.equalTo("The Rate Limit has been reached! Please wait 123456789 milliseconds."));
        assertThat(exception.getAvailableIn(), Matchers.equalTo(123456789L));
    }

}