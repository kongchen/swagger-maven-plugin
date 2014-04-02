package com.github.kongchen.swagger.docgen;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/29/2013
 */
public class GenerateException extends Throwable {
    public GenerateException(String errorMessage) {
        super(errorMessage);
    }

    public GenerateException(Exception e) {
        super(e);
    }
}
