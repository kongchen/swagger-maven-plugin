package com.github.kongchen.swagger.docgen.jaxrs;

import com.github.kongchen.swagger.docgen.reader.JaxrsReader;
import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.core.header.FormDataContentDisposition;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.reflect.TypeUtils;

import javax.ws.rs.BeanParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This extension extracts the parameters inside a {@code @BeanParam} by
 * expanding the target bean type's fields/methods/constructor parameters and
 * recursively feeding them back through the {@link JaxrsReader}.
 *
 * @author chekong on 15/5/9.
 */
public class BeanParamInjectParamExtention extends AbstractSwaggerExtension {

    private static final AccessibleObjectGetter<Field> FIELD_GETTER = new AccessibleObjectGetter<Field>() {
        @Override
        public Field[] get(final Class<?> clazz) {
            return clazz.getDeclaredFields();
        }
    };

    private static final AccessibleObjectGetter<Method> METHOD_GETTER = new AccessibleObjectGetter<Method>() {
        @Override
        public Method[] get(final Class<?> clazz) {
            return clazz.getDeclaredMethods();
        }
    };

    private static final AccessibleObjectGetter<Constructor<?>> CONSTRUCTOR_GETTER = new AccessibleObjectGetter<Constructor<?>>() {
        @Override
        public Constructor<?>[] get(final Class<?> clazz) {
            return clazz.getDeclaredConstructors();
        }
    };

    private final JaxrsReader reader;

    public BeanParamInjectParamExtention(final JaxrsReader reader) {
        this.reader = reader;
    }

    @Override
    public List<Parameter> extractParameters(final List<Annotation> annotations, final Type type, final Set<Type> typesToSkip, final Iterator<SwaggerExtension> chain) {
        final Class<?> cls = TypeUtils.getRawType(type, type);

        if (shouldIgnoreClass(cls) || typesToSkip.contains(type)) {
            // stop the processing chain
            typesToSkip.add(type);
            return Collections.emptyList();
        }
        for (final Annotation annotation : annotations) {
            if (annotation instanceof BeanParam || annotation instanceof InjectParam) {
                return extractParameters(cls, typesToSkip);
            }
        }
        if (chain.hasNext()) {
            return chain.next().extractParameters(annotations, type, typesToSkip, chain);
        }
        return Collections.emptyList();
    }

    private List<Parameter> extractParameters(final Class<?> cls, final Set<Type> typesToSkip) {

        final Collection<TypeWithAnnotations> typesWithAnnotations = new ArrayList<TypeWithAnnotations>();

        for (final Field field : getDeclaredAndInheritedMembers(cls, FIELD_GETTER)) {
            final Type type = field.getGenericType();
            final List<Annotation> annotations = Arrays.asList(field.getAnnotations());
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }

        /*
         * For methods we will only examine setters and will only look at the
         * annotations on the parameter, not the method itself.
         */
        for (final Method method : getDeclaredAndInheritedMembers(cls, METHOD_GETTER)) {

            final Type[] parameterTypes = method.getGenericParameterTypes();
            // skip methods that don't look like setters
            if (parameterTypes.length != 1 || method.getReturnType() != void.class) {
                continue;
            }
            final Type type = parameterTypes[0];
            final List<Annotation> annotations = Arrays.asList(JaxrsReader.findParamAnnotations(method)[0]);
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }

        for (final Constructor<?> constructor : getDeclaredAndInheritedMembers(cls, CONSTRUCTOR_GETTER)) {

            final Type[] parameterTypes = constructor.getGenericParameterTypes();
            final Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

            for (int i = 0; i < parameterTypes.length; i++) {
                final Type type = parameterTypes[i];
                final List<Annotation> annotations = Arrays.asList(parameterAnnotations[i]);
                typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
            }
        }

        final List<Parameter> output = new ArrayList<Parameter>();

        for (final TypeWithAnnotations typeWithAnnotations : typesWithAnnotations) {

            final Type type = typeWithAnnotations.getType();
            final List<Annotation> annotations = typeWithAnnotations.getAnnotations();

            /*
             * Skip the type of the bean itself when recursing into its members
             * in order to avoid a cycle (stack overflow), as crazy as that user
             * code would have to be.
             *
             * There are no tests to prove this works because the test bean
             * classes are shared with SwaggerReaderTest and Swagger's own logic
             * doesn't prevent this problem.
             */
            final Set<Type> recurseTypesToSkip = new HashSet<Type>(typesToSkip);
            recurseTypesToSkip.add(cls);

            output.addAll(reader.getParameters(type, annotations, recurseTypesToSkip));
        }

        return output;
    }

    @Override
    public boolean shouldIgnoreClass(final Class<?> cls) {
        return FormDataContentDisposition.class.equals(cls);
    }

    private <T extends AccessibleObject> List<T> getDeclaredAndInheritedMembers(final Class<?> clazz, final AccessibleObjectGetter<? extends T> getter) {
        final List<T> fields = new ArrayList<T>();
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

    private static final class TypeWithAnnotations {

        private final Type type;
        private final List<Annotation> annotations;

        TypeWithAnnotations(final Type type, final List<Annotation> annotations) {
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
}
