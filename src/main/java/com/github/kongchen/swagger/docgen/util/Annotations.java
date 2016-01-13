package com.github.kongchen.swagger.docgen.util;

import javax.ws.rs.GET;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Annotations {

    public static <T extends Annotation> T get(Method method, Class<T> annotationClass) {
        return method.getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T get(Class<?> givenClass, Class<T> annotationClass) {
        return givenClass.getAnnotation(annotationClass);
    }
}
