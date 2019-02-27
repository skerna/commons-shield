package io.skerna.shield.jwtverifier;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolverAdapter;
import io.skerna.shield.jwks.Jwk;
import io.skerna.shield.jwks.JwkException;
import io.skerna.shield.jwks.JwkProvider;
import io.skerna.shield.jwks.UrlJwkProvider;
import io.skerna.shield.jwtverifier.commons.Base64Url;

import java.math.BigInteger;
import java.net.URL;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class RSAJwksAdapter extends SigningKeyResolverAdapter {
    private JwkProvider provider;
    // Cache keys parsed from OIDC, avoid rework generateKey
    private Cache<String, Key> cacheKeys;

    /**
     * {@link RSAJwksAdapter} provides RSA Public key only verify tokens
     */
    public RSAJwksAdapter(
            URL url,
            Integer connectTimeout,
            Integer readTimeOut,
            Duration timeCacheParserOidcKeys
    ) {
        this.provider  = new UrlJwkProvider(
                url,
                connectTimeout,
                readTimeOut
        );
        this.cacheKeys = CacheBuilder.newBuilder()
                .expireAfterAccess(timeCacheParserOidcKeys)
                .build();
    }

    public RSAJwksAdapter(
            URL url,
            Integer connectTimeout,
            Integer readTimeOut
    ) {
        this(url,connectTimeout,readTimeOut,Duration.ofHours(1));
    }

    @Override
    public Key resolveSigningKey(JwsHeader header, Claims claims) {
        String kid = header.getKeyId();
        try {
            if(kid == null){
                throw new IllegalStateException("kid not found in header jwt");
            }
            // Load from cache
            Key key  = cacheKeys.get(kid, () -> generateKey(kid));
            return key;

        } catch (Exception e) {
            throw new SecurityException(String.format("Can not load jwt public key desde el almacen, para [%s]",kid),e);
        }
    }

    /**
     * Generate Public Key from OIDC wellknow config
     * https://tools.ietf.org/html/rfc8414#page-10
     *
     * Lodad from jwks_uri implemented by
     * @see UrlJwkProvider
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    private Key generateKey(String kid) throws NoSuchAlgorithmException, InvalidKeySpecException, JwkException {
        // Request jwk from provider oidc, autorotatekeys
        Jwk jwk = provider.get(kid);
        Objects.requireNonNull(jwk,"jwk can´t be null");
        if(!jwk.getAlgorithm().toLowerCase().contains("RS".toLowerCase())){
            throw new IllegalStateException(String.format("Only RSA allowed, finded (%s)",jwk.getAlgorithm() ));
        }

        // initialie request data
       // String kid = jwk.getId(); // kid jwt
        String algorithm ="RSA"; // algorithm
        Map<String, Object> additionalAttributes = jwk.getAdditionalAttributes(); // Aditional mod && exp RSA

        String modulusEncoded = (String) additionalAttributes.get("n");
        String exponentEncoded = (String) additionalAttributes.get("e");

        if(modulusEncoded == null || exponentEncoded == null){
            throw new IllegalStateException("Require mod and exponent");
        }

        // Decode base 64
        byte[] moduleDecoded = Base64Url.decode(modulusEncoded);
        byte[] exponeDecoded = Base64Url.decode(exponentEncoded);

        // KeySpecification
        KeySpec spec = new RSAPublicKeySpec(new BigInteger(1, moduleDecoded), new BigInteger(1, exponeDecoded));

        KeyFactory  keyFactory= KeyFactory.getInstance("RSA");
        PublicKey key = keyFactory.generatePublic(spec);

        return key;

    }

    public Cache<String, Key> getCacheKeys() {
        return cacheKeys;
    }

}
