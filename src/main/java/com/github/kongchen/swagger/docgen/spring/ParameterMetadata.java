package com.github.kongchen.swagger.docgen.spring;

import org.springframework.core.style.ToStringCreator;

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

    public String toString() {
        return new ToStringCreator(this).append("type", type).toString();
    }
}
