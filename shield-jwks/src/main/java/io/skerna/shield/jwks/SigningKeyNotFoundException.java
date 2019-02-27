package io.skerna.shield.jwks;

@SuppressWarnings("WeakerAccess")
public class SigningKeyNotFoundException extends JwkException {

    public SigningKeyNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
