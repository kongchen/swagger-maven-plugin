package com.github.kongchen.swagger.docgen.util;

import com.github.kongchen.swagger.docgen.reader.JaxrsReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TypeExtracter {

    private static final AccessibleObjectGetter<Field> FIELD_GETTER = new AccessibleObjectGetter<Field>() {
        @Override
        public Field[] get(Class<?> clazz) {
            return clazz.getDeclaredFields();
        }
    };

    private static final AccessibleObjectGetter<Method> METHOD_GETTER = new AccessibleObjectGetter<Method>() {
        @Override
        public Method[] get(Class<?> clazz) {
            return clazz.getDeclaredMethods();
        }
    };

    private static final AccessibleObjectGetter<Constructor<?>> CONSTRUCTOR_GETTER = new AccessibleObjectGetter<Constructor<?>>() {
        @Override
        public Constructor<?>[] get(Class<?> clazz) {
            return clazz.getDeclaredConstructors();
        }
    };

    public Collection<TypeWithAnnotations> extractTypes(Class<?> cls) {

        ArrayList<TypeWithAnnotations> typesWithAnnotations = new ArrayList<TypeWithAnnotations>();

        typesWithAnnotations.addAll(getPropertyTypes(cls));
        typesWithAnnotations.addAll(getMethodParameterTypes(cls));
        typesWithAnnotations.addAll(getConstructorParameterTypes(cls));

        return typesWithAnnotations;
    }

    private Collection<TypeWithAnnotations> getPropertyTypes(Class<?> clazz) {
        Collection<TypeWithAnnotations> typesWithAnnotations = new ArrayList<TypeWithAnnotations>();
        for (Field field : getDeclaredAndInheritedMembers(clazz, FIELD_GETTER)) {
            Type type = field.getGenericType();
            List<Annotation> annotations = Arrays.asList(field.getAnnotations());
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }

        return typesWithAnnotations;
    }

    private Collection<TypeWithAnnotations> getMethodParameterTypes(Class<?> clazz) {
        Collection<TypeWithAnnotations> typesWithAnnotations = new ArrayList<TypeWithAnnotations>();
        /*
         * For methods we will only examine setters and will only look at the
         * annotations on the parameter, not the method itself.
         */
        for (Method method : getDeclaredAndInheritedMembers(clazz, METHOD_GETTER)) {

            Type[] parameterTypes = method.getGenericParameterTypes();
            // skip methods that don't look like setters
            if (parameterTypes.length != 1 || method.getReturnType() != void.class) {
                continue;
            }
            Type type = parameterTypes[0];
            List<Annotation> annotations = Arrays.asList(JaxrsReader.findParamAnnotations(method)[0]);
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }

        return typesWithAnnotations;
    }

    private Collection<TypeWithAnnotations> getConstructorParameterTypes(Class<?> clazz) {
        Collection<TypeWithAnnotations> typesWithAnnotations = new ArrayList<TypeWithAnnotations>();
        for (Constructor<?> constructor : getDeclaredAndInheritedMembers(clazz, CONSTRUCTOR_GETTER)) {

            Type[] parameterTypes = constructor.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

            for (int i = 0; i < parameterTypes.length; i++) {
                Type type = parameterTypes[i];
                List<Annotation> annotations = Arrays.asList(parameterAnnotations[i]);
                typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
            }
        }

        return typesWithAnnotations;
    }

    private <T extends AccessibleObject> List<T> getDeclaredAndInheritedMembers(Class<?> clazz, AccessibleObjectGetter<? extends T> getter) {
        List<T> fields = new ArrayList<T>();
        Class<?> inspectedClass = clazz;
        while (inspectedClass != null) {
            fields.addAll(Arrays.asList(getter.get(inspectedClass)));
            inspectedClass = inspectedClass.getSuperclass();
        }
        return fields;
    }

    // get rid of this and use lambdas instead once Java 8 is supported
    private interface AccessibleObjectGetter<T extends AccessibleObject> {

        T[] get(Class<?> clazz);
    }
}
