package com.github.kongchen.swagger.docgen.reader;

import org.apache.maven.plugin.logging.Log;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public class SpringExceptionHandlerReader {
    private final Log log;

    private Map<Class<? extends Throwable>, ResponseStatus> exceptionMapping =
            new HashMap<Class<? extends Throwable>, ResponseStatus>();

    public SpringExceptionHandlerReader(final Log log) {
        this.log = log;
    }

    public void processExceptionHandlers(final Set<Class<?>> classes) {
        exceptionMapping = generateExceptionMapping(classes);
    }

    protected Map<Class<? extends Throwable>, ResponseStatus> generateExceptionMapping(final Set<Class<?>> classes) {
        final Map<Class<? extends Throwable>, ResponseStatus> result =
                new HashMap<Class<? extends Throwable>, ResponseStatus>();

        log.debug(String.format("Looking for classes with @ControllerAdvice annotation"));
        for (final Class clazz: classes) {
            final ControllerAdvice advice = findAnnotation(clazz, ControllerAdvice.class);
            if (advice == null) {
                continue;
            }

            log.debug(String.format("%s is annotated as @ControllerAdvice", clazz.getName()));

            for (final Method method: clazz.getMethods()) {
                final ExceptionHandler handler = findAnnotation(method, ExceptionHandler.class);
                if (handler == null) {
                    log.debug(String.format("@ExceptionHandler is missing on %s method, skipping", method));
                    continue;
                }

                final ResponseStatus responseStatus = findAnnotation(method, ResponseStatus.class);
                if (responseStatus == null) {
                    log.debug(String.format("@ResponseStatus is missing on %s method, skipping", method));
                    continue;
                }

                final Class[] exceptionClasses = handler.value();
                for (final Class exceptionClass: exceptionClasses) {
                    log.debug(String.format("%s will be mapped to %s", exceptionClass, responseStatus));
                    result.put(exceptionClass, responseStatus);
                }
            }

        }
        return result;
    }

    protected List<ResponseStatus> getResponseStatusesFromExceptions(final Method method) {
        final List<ResponseStatus> result = new LinkedList<ResponseStatus>();
        for (final Class exceptionClass: method.getExceptionTypes()) {
            ResponseStatus responseStatus = exceptionMapping.get(exceptionClass);

            // fallback to exception own annotation
            if (null == responseStatus) {
                responseStatus = findMergedAnnotation(exceptionClass, ResponseStatus.class);
            }

            if (null != responseStatus) {
                result.add(responseStatus);
            }
        }
        return result;
    }
}
