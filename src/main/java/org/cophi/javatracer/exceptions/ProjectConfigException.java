package org.cophi.javatracer.exceptions;

public class ProjectConfigException extends RuntimeException {

        public ProjectConfigException() {
            super();
        }

        public ProjectConfigException(final String message) {
            super(message);
        }

        public ProjectConfigException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public ProjectConfigException(final Throwable cause) {
            super(cause);
        }

        protected ProjectConfigException(final String message, final Throwable cause,
            final boolean enableSuppression, final boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
}
