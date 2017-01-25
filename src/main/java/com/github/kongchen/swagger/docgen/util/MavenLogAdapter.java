package com.github.kongchen.swagger.docgen.util;


import org.apache.maven.plugin.logging.Log;

public class MavenLogAdapter implements LogAdapter {

    private final Log log;

    public MavenLogAdapter(Log log) {
        this.log = log;
    }

    @Override
    public void info(String s) {
        log.info(s);
    }

    @Override
    public void error(String s) {
        log.error(s);
    }

    @Override
    public void error(String message, Throwable e) {
        log.error(message, e);
    }

    @Override
    public void warn(String s) {
        log.warn(s);
    }
}
