package com.github.kongchen.swagger.docgen;

import java.lang.reflect.Method;

import org.apache.maven.plugin.logging.Log;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class LogAdapter {
    Object logger;

    public LogAdapter(org.slf4j.Logger logger) {
        this.logger = logger;

    }

    public LogAdapter(Log log) {
        this.logger = log;
    }

    private void invoke(String methodName, String s) {
        try {
            Method[] infoMethods = logger.getClass().getDeclaredMethods();
            for (Method m : infoMethods) {
                if (!m.getName().equals(methodName)) continue;
                Class<?>[] types = m.getParameterTypes();
                if (types.length == 1 && CharSequence.class.isAssignableFrom(types[0])) {
                    m.invoke(logger, s);
                    return;
                }
            }

        } catch (Exception e) {
            System.out.println(s);
        }
    }

    public void info(String s) {
        invoke("info", s);
    }

    public void error(String s) {
        invoke("error", s);
    }

    public void warn(String s) {
        invoke("warn", s);
    }
}
