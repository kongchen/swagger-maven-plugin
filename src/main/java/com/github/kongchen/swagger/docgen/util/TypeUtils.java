package com.github.kongchen.swagger.docgen.util;

import java.lang.reflect.Type;

import io.swagger.converter.ModelConverters;
import io.swagger.models.properties.Property;

public class TypeUtils {

    public static boolean isPrimitive(Type cls) {
        boolean isPrimitive = false;

        Property property = ModelConverters.getInstance().readAsProperty(cls);
        if (property == null) {
            isPrimitive = false;
        } else if ("integer".equals(property.getType())) {
            isPrimitive = true;
        } else if ("string".equals(property.getType())) {
            isPrimitive = true;
        } else if ("number".equals(property.getType())) {
            isPrimitive = true;
        } else if ("boolean".equals(property.getType())) {
            isPrimitive = true;
        } else if ("array".equals(property.getType())) {
            isPrimitive = true;
        } else if ("file".equals(property.getType())) {
            isPrimitive = true;
        }
        return isPrimitive;
    }
}
