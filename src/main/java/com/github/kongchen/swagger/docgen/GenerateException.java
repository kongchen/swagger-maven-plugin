package com.github.kongchen.swagger.docgen;

/**
 * @author chekong
 *         05/29/2013
 */
public class GenerateException extends Exception {

    private static final long serialVersionUID = -1641016437077276797L;

    public GenerateException(final String errorMessage, final Throwable cause) {
        super(errorMessage, cause);
    }

    public GenerateException(final String errorMessage) {
        super(errorMessage);
    }

    public GenerateException(final Exception e) {
        super(e);
    }
}

