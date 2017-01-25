package com.github.kongchen.swagger.docgen.spring;

import java.lang.annotation.Annotation;

final class ParameterMetadata {

    private final Class<?> type;

    private final Annotation[] annotations;

    public ParameterMetadata(Class<?> type, Annotation[] annotations) {
        this.type = type;
        this.annotations = annotations;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }

    public Class<?> getType() {
        return type;
    }
}
