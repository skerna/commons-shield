package io.skerna.shield.jwks;

@SuppressWarnings("WeakerAccess")
public class JwkException extends Exception {

    public JwkException(String message) {
        super(message);
    }

    public JwkException(String message, Throwable cause) {
        super(message, cause);
    }

}
