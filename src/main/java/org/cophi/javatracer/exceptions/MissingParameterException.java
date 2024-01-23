package org.cophi.javatracer.exceptions;

public class MissingParameterException extends IllegalArgumentException {

    public MissingParameterException() {
        super();
    }

    public MissingParameterException(final String message) {
        super(message);
    }

    public MissingParameterException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MissingParameterException(final Throwable cause) {
        super(cause);
    }
}
