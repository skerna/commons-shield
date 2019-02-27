package io.skerna.shield.jwks;

import java.net.MalformedURLException;
import java.net.URL;

public class Demo {
    public static void main(String[] args) throws JwkException, MalformedURLException {
        URL url = new URL("http","localhost",8080,"/auth/realms/master/protocol/openid-connect/certs");
        JwkProvider provider = new UrlJwkProvider(url);
        Jwk jwk = provider.get("FX6JPwanh8BAyGdEnexVDcrmuP-1uqD04IMBTtZssTQ"); //throws Exception when not found or can't get
        System.out.println(jwk.getAdditionalAttributes());
    }
}
