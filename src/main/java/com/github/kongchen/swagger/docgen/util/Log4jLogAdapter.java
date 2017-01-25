package com.github.kongchen.swagger.docgen.util;

import org.apache.log4j.Logger;

public class Log4jLogAdapter implements LogAdapter {

    private final Logger logger;

    public Log4jLogAdapter(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String s) {
        logger.info(s);
    }

    @Override
    public void error(String s) {
        logger.error(s);
    }

    @Override
    public void error(String message, Throwable e) {
        logger.error(message, e);
    }

    @Override
    public void warn(String s) {
        logger.warn(s);
    }
}
