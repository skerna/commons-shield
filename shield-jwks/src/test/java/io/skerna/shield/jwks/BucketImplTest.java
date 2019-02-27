package io.skerna.shield.jwks;


import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BucketImplTest {
    private static final long RATE = 500L;
    private static final long SIZE = 5L;

    @Test
    public void shouldThrowOnCreateWithNegativeSize() throws Exception {
        final IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    new BucketImpl(-1, 10, TimeUnit.SECONDS);
                }
        );
        Assertions.assertEquals("Invalid bucket size.",thrown.getMessage());
    }

    @Test
    public void shouldThrowOnCreateWithNegativeRate() throws Exception {
        final IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    new BucketImpl(10, -1, TimeUnit.SECONDS);
                }
        );
        Assertions.assertEquals("Invalid bucket refill rate.",thrown.getMessage());
    }

    @Test
    public void shouldThrowWhenLeakingMoreThanBucketSize() throws Exception {
        final IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.SECONDS);
                    bucket.willLeakIn(SIZE + 1);
                }
        );

        Assertions.assertEquals(format("Cannot consume %d tokens when the BucketImpl size is %d!", SIZE + 1, SIZE),thrown.getMessage());
    }

    @Test
    public void shouldThrowWhenConsumingMoreThanBucketSize() throws Exception {
        final IllegalArgumentException thrown = Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.SECONDS);
                    bucket.consume(SIZE + 1);
                }
        );
        Assertions.assertEquals(format("Cannot consume %d tokens when the BucketImpl size is %d!", SIZE + 1, SIZE),thrown.getMessage());

    }

    @Test
    public void shouldCreateFullBucket() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());
        assertThat(bucket.willLeakIn(SIZE), Matchers.equalTo(0L));
        assertThat(bucket.willLeakIn(), Matchers.equalTo(0L));

    }

    @Test
    public void shouldAddOneTokenPerRate() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());
        assertThat(bucket.consume(SIZE), Matchers.equalTo(true));
        assertThat(bucket.consume(), Matchers.equalTo(false));

        //wait for 1 token and consume it
        assertThat(bucket.willLeakIn(), Matchers.allOf(Matchers.greaterThan(0L), Matchers.lessThanOrEqualTo(RATE)));
        pause(RATE);
        assertThat(bucket.consume(), Matchers.equalTo(true));

        //wait for 5 tokens and consume them
        assertThat(bucket.willLeakIn(SIZE), Matchers.allOf(Matchers.greaterThan((SIZE - 1) * RATE), Matchers.lessThanOrEqualTo(SIZE * RATE)));
        pause(SIZE * RATE);
        assertThat(bucket.consume(SIZE), Matchers.equalTo(true));

        //expect to wait 1 token rate
        assertThat(bucket.willLeakIn(), Matchers.allOf(Matchers.greaterThan(0L), Matchers.lessThanOrEqualTo(RATE)));
        assertThat(bucket.consume(), Matchers.equalTo(false));
    }

    @Test
    public void shouldNotAddMoreTokensThatTheBucketSize() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());
        assertThat(bucket.willLeakIn(SIZE), Matchers.equalTo(0L));

        //Give some time to fill the already full bucket
        pause(SIZE * RATE * 2);
        assertThat(bucket.consume(SIZE), Matchers.equalTo(true));
        assertThat(bucket.consume(), Matchers.equalTo(false));
    }

    @Test
    public void shouldConsumeAllBucketTokens() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());
        assertThat(bucket.consume(SIZE), Matchers.equalTo(true));
        assertThat(bucket.consume(), Matchers.equalTo(false));
    }

    @Test
    public void shouldConsumeByOneToken() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());
        //Consume 5 tokens
        assertThat(bucket.consume(), Matchers.equalTo(true));
        assertThat(bucket.consume(), Matchers.equalTo(true));
        assertThat(bucket.consume(), Matchers.equalTo(true));
        assertThat(bucket.consume(), Matchers.equalTo(true));
        assertThat(bucket.consume(), Matchers.equalTo(true));
        //should not consume a 6th token
        assertThat(bucket.consume(), Matchers.equalTo(false));
    }

    @Test
    public void shouldCalculateRemainingLeakTimeForOneToken() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());
        //Consume 5 tokens
        assertThat(bucket.consume(5), Matchers.equalTo(true));
        assertThat(bucket.willLeakIn(), Matchers.allOf(Matchers.greaterThan(0L), Matchers.lessThanOrEqualTo(RATE)));
        // wait half rate time and check if the wait time is correct
        pause(RATE / 2);
        assertThat(bucket.willLeakIn(), Matchers.allOf(Matchers.greaterThan(0L), Matchers.lessThanOrEqualTo(RATE / 2)));
    }

    @Test
    public void shouldCalculateRemainingLeakTimeForManyTokens() throws Exception {
        Bucket bucket = new BucketImpl(SIZE, RATE, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());
        //Consume 3 tokens
        assertThat(bucket.consume(3), Matchers.equalTo(true));

        //Expected to wait 3 * RATE time at most to be able to consume 5 tokens
        assertThat(bucket.willLeakIn(5), Matchers.allOf(Matchers.greaterThanOrEqualTo(RATE * 2), Matchers.lessThanOrEqualTo(RATE * 3)));
        pause(RATE * 3);
        assertThat(bucket.willLeakIn(5), Matchers.allOf(Matchers.greaterThanOrEqualTo(0L), Matchers.lessThanOrEqualTo(RATE)));
    }


    @Test
    public void shouldCarryDeltaWhenManyTokensAreRequested() throws Exception {
        Bucket bucket = new BucketImpl(5, 1000, TimeUnit.MILLISECONDS);
        MatcherAssert.assertThat(bucket, Matchers.notNullValue());

        //Consume all tokens. Expect to wait 5 seconds for refill
        assertThat(bucket.consume(5), Matchers.equalTo(true));
        assertThat(bucket.willLeakIn(5), Matchers.allOf(Matchers.greaterThanOrEqualTo(4900L), Matchers.lessThanOrEqualTo(5000L)));

        //wait 1500ms to have 1 token.
        pause(1500);
        //Consume 1 and expect to wait 500 + 4000 ms if we want to consume 5 again.
        assertThat(bucket.consume(), Matchers.equalTo(true));
        assertThat(bucket.willLeakIn(5), Matchers.allOf(Matchers.greaterThanOrEqualTo(4400L), Matchers.lessThanOrEqualTo(4500L)));
    }

    private void pause(long ms) throws InterruptedException {
        System.out.println(format("Waiting %d ms..", ms));
        Thread.sleep(ms);
    }
}