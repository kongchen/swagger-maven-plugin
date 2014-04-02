package com.github.kongchen.swagger.docgen;

/**
 * Facade for various logging utilies.
 *
 * @author: davidecavestro
 * 03/02/2014
 */
public interface LogAdapter {

    void info(String s);

    void error(String s);

    void warn(String s);
}
