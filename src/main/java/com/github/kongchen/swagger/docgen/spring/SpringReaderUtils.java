package com.github.kongchen.swagger.docgen.spring;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class SpringReaderUtils {
    static Class<?> getGenericSubtype(Class<?> clazz, Type t) {
        if (!(clazz.getName().equals("void") || t.toString().equals("void"))) {
            try {
                ParameterizedType paramType = (ParameterizedType) t;
                Type[] argTypes = paramType.getActualTypeArguments();
                if (argTypes.length > 0) {
                    return (Class<?>) argTypes[0];
                }
            } catch (ClassCastException e) {
                //FIXME: find out why this happens to only certain types
            }
        }
        return clazz;
    }
}
