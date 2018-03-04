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
 * exploding the target bean type's fields/methods and recursively feeding them
 * back through the {@link JaxrsReader}.
 * 
 * @author chekong on 15/5/9.
 */
public class BeanParamInjectParamExtention extends AbstractSwaggerExtension {
    
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

    private final JaxrsReader reader;
    
    public BeanParamInjectParamExtention(JaxrsReader reader) {
        this.reader = reader;
    }
    
    @Override
    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip, Iterator<SwaggerExtension> chain) {
        Class<?> cls = TypeUtils.getRawType(type, type);

        if (shouldIgnoreClass(cls) || typesToSkip.contains(type)) {
            // stop the processing chain
            typesToSkip.add(type);
            return Collections.emptyList();
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof BeanParam || annotation instanceof InjectParam) {
                return extractParameters(cls, typesToSkip);
            }
        }
        if (chain.hasNext()) {
            return chain.next().extractParameters(annotations, type, typesToSkip, chain);
        }
        return Collections.emptyList();
    }

    private List<Parameter> extractParameters(Class<?> cls, Set<Type> typesToSkip) {
        
        Collection<TypeWithAnnotations> typesWithAnnotations = new ArrayList<TypeWithAnnotations>();
        
        for (Field field : getDeclaredAndInheritedMembers(cls, FIELD_GETTER)) {
            Type type = field.getGenericType();
            List<Annotation> annotations = Arrays.asList(field.getAnnotations());
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }
        
        /*
         * For methods we will only examine setters and will only look at the
         * annotations on the parameter, not the method itself.
         */
        for (Method method : getDeclaredAndInheritedMembers(cls, METHOD_GETTER)) {

            Type[] parameterTypes = method.getGenericParameterTypes();
            // skip methods that don't look like setters
            if (parameterTypes.length != 1 || method.getReturnType() != void.class) {
                continue;
            }
            Type type = parameterTypes[0];
            List<Annotation> annotations = Arrays.asList(JaxrsReader.findParamAnnotations(method)[0]);
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }
        
        for (Constructor<?> constructor : getDeclaredAndInheritedMembers(cls, CONSTRUCTOR_GETTER)) {
            
            Type[] parameterTypes = constructor.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();
            
            for (int i = 0; i < parameterTypes.length; i++) {
                Type type = parameterTypes[i];
                List<Annotation> annotations = Arrays.asList(parameterAnnotations[i]);
                typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
            }
        }
        
        List<Parameter> output = new ArrayList<Parameter>();
        
        for (TypeWithAnnotations typeWithAnnotations : typesWithAnnotations) {
            
            Type type = typeWithAnnotations.getType();
            List<Annotation> annotations = typeWithAnnotations.getAnnotations();

            /*
             * Skip the type of the bean itself when recursing into its members
             * in order to avoid a cycle (stack overflow), as crazy as that user
             * code would have to be.
             * 
             * There are no tests to prove this works because the test bean
             * classes are shared with SwaggerReaderTest and Swagger's own logic
             * doesn't prevent this problem.
             */
            Set<Type> recurseTypesToSkip = new HashSet<Type>(typesToSkip);
            recurseTypesToSkip.add(cls);
            
            output.addAll(reader.getParameters(type, annotations, recurseTypesToSkip));
        }

        return output;
    }

    @Override
    public boolean shouldIgnoreClass(Class<?> cls) {
        return FormDataContentDisposition.class.equals(cls);
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

    private static final class TypeWithAnnotations {

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
}
