package com.github.kongchen.swagger.docgen.util;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public interface LogAdapter {

    void info(String s);

    void error(String s);

    void error(String message, Throwable e);

    void warn(String s);

    void debug(String message);
}
