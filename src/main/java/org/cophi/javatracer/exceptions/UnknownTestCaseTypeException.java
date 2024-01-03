package org.cophi.javatracer.exceptions;

import java.io.Serial;

public class UnknownTestCaseTypeException extends Exception {

        @Serial
        private static final long serialVersionUID = 1L;

        public UnknownTestCaseTypeException(String message) {
            super(message);
        }

        public UnknownTestCaseTypeException(String message, Throwable cause) {
            super(message, cause);
        }

        public UnknownTestCaseTypeException(Throwable cause) {
            super(cause);
        }

}
