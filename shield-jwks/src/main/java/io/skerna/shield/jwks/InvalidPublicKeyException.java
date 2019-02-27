package io.skerna.shield.jwks;

@SuppressWarnings("WeakerAccess")
public class InvalidPublicKeyException extends JwkException {

    public InvalidPublicKeyException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
