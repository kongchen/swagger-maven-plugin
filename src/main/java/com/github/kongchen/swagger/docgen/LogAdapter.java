package com.github.kongchen.swagger.docgen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.apache.maven.plugin.logging.Log;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class LogAdapter {
    Object logger;

    public LogAdapter(Logger logger) {
        this.logger = logger;

    }

    public LogAdapter(Log log) {
        this.logger = log;
    }

    private void invoke(String methodName, String s) {
        try {
            Method m = null;
            if (logger instanceof Logger) {
                m = logger.getClass().getSuperclass().getDeclaredMethod(methodName, Object.class);

            } else if (logger instanceof Log) {
                m = logger.getClass().getDeclaredMethod(methodName, CharSequence.class);
            }
            if (m != null) {
                m.invoke(logger, s);
            }
        } catch (NoSuchMethodException e) {
            System.out.print(s);
        } catch (InvocationTargetException e) {
            System.out.print(s);
        } catch (IllegalAccessException e) {
            System.out.print(s);
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
