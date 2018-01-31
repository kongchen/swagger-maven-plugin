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

    public SpringExceptionHandlerReader(Log log) {
        this.log = log;
    }

    public void processExceptionHandlers(Set<Class<?>> classes) {
        exceptionMapping = generateExceptionMapping(classes);
    }

    protected Map<Class<? extends Throwable>, ResponseStatus> generateExceptionMapping(Set<Class<?>> classes) {
        Map<Class<? extends Throwable>, ResponseStatus> result =
                new HashMap<Class<? extends Throwable>, ResponseStatus>();

        log.debug(String.format("Looking for classes with @ControllerAdvice annotation"));
        for (Class clazz: classes) {
            ControllerAdvice advice = findAnnotation(clazz, ControllerAdvice.class);
            if (advice == null) {
                continue;
            }

            log.debug(String.format("%s is annotated as @ControllerAdvice", clazz.getName()));

            for (Method method: clazz.getMethods()) {
                ExceptionHandler handler = findAnnotation(method, ExceptionHandler.class);
                if (handler == null) {
                    log.debug(String.format("@ExceptionHandler is missing on %s method, skipping", method));
                    continue;
                }

                ResponseStatus responseStatus = findAnnotation(method, ResponseStatus.class);
                if (responseStatus == null) {
                    log.debug(String.format("@ResponseStatus is missing on %s method, skipping", method));
                    continue;
                }

                Class[] exceptionClasses = handler.value();
                for (Class exceptionClass: exceptionClasses) {
                    log.debug(String.format("%s will be mapped to %s", exceptionClass, responseStatus));
                    result.put(exceptionClass, responseStatus);
                }
            }

        }
        return result;
    }

    protected List<ResponseStatus> getResponseStatusesFromExceptions(Method method) {
        List<ResponseStatus> result = new LinkedList<ResponseStatus>();
        for (Class exceptionClass: method.getExceptionTypes()) {
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
