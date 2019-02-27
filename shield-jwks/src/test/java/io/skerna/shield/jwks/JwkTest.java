package io.skerna.shield.jwks;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;

public class JwkTest {

    private static final String RS_256 = "RS256";
    private static final String RSA = "RSA";
    private static final String SIG = "sig";
    private static final String THUMBPRINT = "THUMBPRINT";
    private static final String MODULUS = "vGChUGMTWZNfRsXxd-BtzC4RDYOMqtIhWHol--HNib5SgudWBg6rEcxvR6LWrx57N6vfo68wwT9_FHlZpaK6NXA_dWFW4f3NftfWLL7Bqy90sO4vijM6LMSE6rnl5VB9_Gsynk7_jyTgYWdTwKur0YRec93eha9oCEXmy7Ob1I2dJ8OQmv2GlvA7XZalMxAq4rFnXLzNQ7hCsHrUJP1p7_7SolWm9vTokkmckzSI_mAH2R27Z56DmI7jUkL9fLU-jz-fz4bkNg-mPz4R-kUmM_ld3-xvto79BtxJvOw5qqtLNnRjiDzoqRv-WrBdw5Vj8Pvrg1fwscfVWHlmq-1pFQ";
    private static final String EXPONENT = "AQAB";
    private static final String CERT_CHAIN = "CERT_CHAIN";
    private static final List<String> KEY_OPS_LIST = Lists.newArrayList("sign");
    private static final String KEY_OPS_STRING = "sign";


    @Test
    public void shouldBuildWithMap() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getId(), Matchers.equalTo(kid));
        assertThat(jwk.getAlgorithm(), Matchers.equalTo(RS_256));
        assertThat(jwk.getType(), Matchers.equalTo(RSA));
        assertThat(jwk.getUsage(), Matchers.equalTo(SIG));
        assertThat(jwk.getOperationsAsList(), Matchers.equalTo(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), Matchers.is(KEY_OPS_STRING));
        assertThat(jwk.getCertificateThumbprint(), Matchers.equalTo(THUMBPRINT));
        assertThat(jwk.getCertificateChain(), Matchers.contains(CERT_CHAIN));
    }

    @Test
    public void shouldReturnPublicKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), Matchers.notNullValue());
        assertThat(jwk.getOperationsAsList(), Matchers.is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), Matchers.is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPublicKeyForStringKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_STRING);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), Matchers.notNullValue());
        assertThat(jwk.getOperationsAsList(), Matchers.is(KEY_OPS_LIST));
        assertThat(jwk.getOperations(), Matchers.is(KEY_OPS_STRING));
    }

    @Test
    public void shouldReturnPublicKeyForNullKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, null);
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), Matchers.notNullValue());
        assertThat(jwk.getOperationsAsList(), Matchers.nullValue());
        assertThat(jwk.getOperations(), Matchers.nullValue());
    }

    @Test
    public void shouldReturnPublicKeyForEmptyKeyOpsParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, Lists.newArrayList());
        Jwk jwk = Jwk.fromValues(values);

        assertThat(jwk.getPublicKey(), Matchers.notNullValue());
        assertThat(jwk.getOperationsAsList(), Matchers.notNullValue());
        assertThat(jwk.getOperationsAsList().size(), Matchers.equalTo(0));
        assertThat(jwk.getOperations(), Matchers.nullValue());
    }

    @Test
    public void shouldReturnNullForNonRSAKey() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = nonRSAValues(kid);
        Jwk jwk = Jwk.fromValues(values);
        assertThat(jwk.getPublicKey(), Matchers.nullValue());
    }
    
    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingKidParam() throws Exception {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    final String kid = randomKeyId();
                    Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
                    values.remove("kid");
                    Jwk.fromValues(values);
                }
        );
    }

    @Test
    public void shouldThrowInvalidArgumentExceptionOnMissingKtyParam() throws Exception {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                ()->{
                    final String kid = randomKeyId();
                    Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
                    values.remove("kty");
                    Jwk.fromValues(values);
                }
        );

    }

    @Test
    public void shouldReturnKeyWithMissingAlgParam() throws Exception {
        final String kid = randomKeyId();
        Map<String, Object> values = publicKeyValues(kid, KEY_OPS_LIST);
        values.remove("alg");
        Jwk jwk = Jwk.fromValues(values);
        assertThat(jwk.getPublicKey(), Matchers.notNullValue());
    }

    private static String randomKeyId() {
        byte[] bytes = new byte[50];
        new SecureRandom().nextBytes(bytes);
        return Base64.encodeBase64String(bytes);
    }

    private static Map<String, Object> nonRSAValues(String kid) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", "AES_256");
        values.put("kty", "AES");
        values.put("use", SIG);
        values.put("kid", kid);
        return values;
    }

    private static Map<String, Object> publicKeyValues(String kid, Object keyOps) {
        Map<String, Object> values = Maps.newHashMap();
        values.put("alg", RS_256);
        values.put("kty", RSA);
        values.put("use", SIG);
        values.put("key_ops", keyOps);
        values.put("x5c", Lists.newArrayList(CERT_CHAIN));
        values.put("x5t", THUMBPRINT);
        values.put("kid", kid);
        values.put("n", MODULUS);
        values.put("e", EXPONENT);
        return values;
    }
}
