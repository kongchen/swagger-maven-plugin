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
        
        for (Field field : getDeclaredAndInheritedFields(cls)) {
            Type type = field.getGenericType();
            List<Annotation> annotations = Arrays.asList(field.getAnnotations());
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }
        
        /*
         * For methods we will only examine setters and will only look at the
         * annotations on the parameter, not the method itself.
         */
        for (Method method : getDeclaredAndInheritedMethods(cls)) {

            Type[] parameterTypes = method.getGenericParameterTypes();
            // skip methods that don't look like setters
            if (parameterTypes.length != 1 || method.getReturnType() != void.class) {
                continue;
            }
            Type type = parameterTypes[0];
            List<Annotation> annotations = Arrays.asList(JaxrsReader.findParamAnnotations(method)[0]);
            typesWithAnnotations.add(new TypeWithAnnotations(type, annotations));
        }
        
        List<Parameter> output = new ArrayList<Parameter>();
        
        for (TypeWithAnnotations typeWithAnnotations : typesWithAnnotations) {
            
            Type type = typeWithAnnotations.getType();
            List<Annotation> annotations = typeWithAnnotations.getAnnotations();

            /*
             * Skip the type of the bean itself when recursing into its members in
             * order to avoid cycles, as crazy as that user code would have to be.
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

    private List<Field> getDeclaredAndInheritedFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<Field>();
        Class<?> inspectedClass = clazz;
        while (inspectedClass != null) {
            fields.addAll(Arrays.asList(inspectedClass.getDeclaredFields()));
            inspectedClass = inspectedClass.getSuperclass();
        }
        return fields;
    }

    private List<Method> getDeclaredAndInheritedMethods(Class<?> clazz) {
        List<Method> methods = new ArrayList<Method>();
        Class<?> inspectedClass = clazz;
        while (inspectedClass != null) {
            methods.addAll(Arrays.asList(inspectedClass.getDeclaredMethods()));
            inspectedClass = inspectedClass.getSuperclass();
        }
        return methods;
    }

    private static final class TypeWithAnnotations {

        final Type type;
        final List<Annotation> annotations;
        
        TypeWithAnnotations(Type type, List<Annotation> annotations) {
            this.type = type;
            this.annotations = annotations;
        }
        
        Type getType() {
            return type;
        }
        
        List<Annotation> getAnnotations() {
            return annotations;
        }
    }
}
