package com.github.kongchen.swagger.docgen.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class Annotations {

    public static <T extends Annotation> T get(Method method, Class<T> annotationClass) {
        T result = method.getAnnotation(annotationClass);
        if (result == null) {
            Class currentClass = method.getDeclaringClass();
            if (currentClass != Object.class) {
                Class superClass = currentClass.getSuperclass();
                Method superClassMethod = null;
                try {
                    superClassMethod = superClass.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException e) {

                }
                if (superClassMethod != null) {
                    return get(superClassMethod, annotationClass);
                }
            }
        }
        return result;
    }

    public static <T extends Annotation> T get(Class<?> givenClass, Class<T> annotationClass) {
        T result = givenClass.getAnnotation(annotationClass);
        if (result == null) {
            if (givenClass != Object.class) {
                Class superClass = givenClass.getSuperclass();
                return (T) get(superClass, annotationClass);
            }
        }
        return result;
    }
}
