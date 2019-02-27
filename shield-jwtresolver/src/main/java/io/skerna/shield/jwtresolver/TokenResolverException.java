package io.skerna.shield.jwtresolver;

public class TokenResolverException extends RuntimeException {
    public TokenResolverException() {
    }

    public TokenResolverException(String s) {
        super(s);
    }

    public TokenResolverException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TokenResolverException(Throwable throwable) {
        super(throwable);
    }

    public TokenResolverException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
