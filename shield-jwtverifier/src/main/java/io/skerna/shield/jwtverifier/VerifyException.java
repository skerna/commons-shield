package io.skerna.shield.jwtverifier;

public class VerifyException extends RuntimeException{
    public VerifyException() {
    }

    public VerifyException(String s) {
        super(s);
    }

    public VerifyException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public VerifyException(Throwable throwable) {
        super(throwable);
    }

    public VerifyException(String s, Throwable throwable, boolean b, boolean b1) {
        super(s, throwable, b, b1);
    }
}
