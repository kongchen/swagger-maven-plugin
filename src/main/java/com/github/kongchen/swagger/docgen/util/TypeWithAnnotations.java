package com.github.kongchen.swagger.docgen.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

public class TypeWithAnnotations {

    private final Type type;
    private final List<Annotation> annotations;

    TypeWithAnnotations(Type type, List<Annotation> annotations) {
        this.type = type;
        this.annotations = annotations;
    }

    public Type getType() {
        return type;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
