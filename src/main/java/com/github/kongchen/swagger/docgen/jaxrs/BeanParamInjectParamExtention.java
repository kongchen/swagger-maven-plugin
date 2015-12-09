package com.github.kongchen.swagger.docgen.jaxrs;

import com.sun.jersey.api.core.InjectParam;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.util.AllowableValues;
import io.swagger.util.AllowableValuesUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.reflections.util.Utils;

import javax.ws.rs.BeanParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by chekong on 15/5/9.
 */
public class BeanParamInjectParamExtention extends AbstractSwaggerExtension implements SwaggerExtension {

    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip, Iterator<SwaggerExtension> chain) {
        Class<?> cls = TypeUtils.getRawType(type, type);

        List<Parameter> output = new ArrayList<Parameter>();
        if (shouldIgnoreClass(cls) || typesToSkip.contains(type)) {
            // stop the processing chain
            typesToSkip.add(type);
            return output;
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof BeanParam || annotation instanceof InjectParam) {
                return extractParameters(cls);

            }
        }
        if (chain.hasNext())
            return chain.next().extractParameters(annotations, type, typesToSkip, chain);
        return null;
    }

    private List<Parameter> extractParameters(Class<?> cls) {
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (AccessibleObject f : getDeclaredAndInheritedFieldsAndMethods(cls)) {
            SerializableParameter parameter = null;

            int i = 0, apiParaIdx = -1;

            for (Annotation annotation : f.getAnnotations()) {
                if (annotation instanceof ApiParam && !((ApiParam) annotation).hidden()) {
                    apiParaIdx = i;
                }
                i++;
                parameter = JaxrsParameterExtension.getParameter(cls, parameter, annotation);

            }
            if (parameter != null) {
                if (apiParaIdx != -1) {
                    ApiParam param = (ApiParam) f.getAnnotations()[apiParaIdx];
                    parameter.setDescription(param.value());
                    parameter.setRequired(param.required());
                    parameter.setAccess(param.access());

                    AllowableValues allowableValues = AllowableValuesUtils.create(param.allowableValues());
                    if (allowableValues != null) {
                        Map<PropertyBuilder.PropertyId, Object> args = allowableValues.asPropertyArguments();
                        if (args.containsKey(PropertyBuilder.PropertyId.ENUM)) {
                            parameter.setEnum((List<String>) args.get(PropertyBuilder.PropertyId.ENUM));
                        }
                    }

                    if (!Utils.isEmpty(param.name())) {
                        parameter.setName(param.name());
                    }
                }
                parameters.add(parameter);
            }
        }

        return parameters;
    }

    public boolean shouldIgnoreClass(Class<?> cls) {
        return com.sun.jersey.core.header.FormDataContentDisposition.class.equals(cls);
    }

    private List<AccessibleObject> getDeclaredAndInheritedFieldsAndMethods(Class<?> c) {
        List<AccessibleObject> accessibleObjects = new ArrayList<AccessibleObject>();
        recurseGetDeclaredAndInheritedFields(c, accessibleObjects);
        recurseGetDeclaredAndInheritedMethods(c, accessibleObjects);
        return accessibleObjects;
    }

    private void recurseGetDeclaredAndInheritedFields(Class<?> c, List<AccessibleObject> fields) {
        fields.addAll(Arrays.asList(c.getDeclaredFields()));
        Class<?> superClass = c.getSuperclass();
        if (superClass != null) {
            recurseGetDeclaredAndInheritedFields(superClass, fields);
        }
    }

    private void recurseGetDeclaredAndInheritedMethods(Class<?> c, List<AccessibleObject> methods) {
        methods.addAll(Arrays.asList(c.getDeclaredMethods()));
        Class<?> superClass = c.getSuperclass();
        if (superClass != null) {
            recurseGetDeclaredAndInheritedMethods(superClass, methods);
        }
    }
}
