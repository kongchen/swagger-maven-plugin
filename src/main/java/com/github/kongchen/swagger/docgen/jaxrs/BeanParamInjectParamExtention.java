package com.github.kongchen.swagger.docgen.jaxrs;

import com.sun.jersey.api.core.InjectParam;
import com.sun.jersey.core.header.FormDataContentDisposition;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.PropertyBuilder;
import io.swagger.util.AllowableValues;
import io.swagger.util.AllowableValuesUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;

import javax.ws.rs.BeanParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chekong on 15/5/9.
 */
public class BeanParamInjectParamExtention extends AbstractSwaggerExtension {

    @Override
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
        if (chain.hasNext()) {
            return chain.next().extractParameters(annotations, type, typesToSkip, chain);
        }
        return Collections.emptyList();
    }

    private List<Parameter> extractParameters(Class<?> cls) {
        List<Parameter> parameters = new ArrayList<Parameter>();

        for (AccessibleObject accessibleObject : getDeclaredAndInheritedFieldsAndMethods(cls)) {
            SerializableParameter parameter = null;

            int i = 0;
            int apiParaIdx = -1;
            boolean hidden = false;

            for (Annotation annotation : accessibleObject.getAnnotations()) {
                if (annotation instanceof ApiParam) {
                    if (((ApiParam) annotation).hidden()) {
                        hidden = true;
                    } else {
                        apiParaIdx = i;
                    }
                }
                i++;
                Type paramType = extractType(accessibleObject, cls);
                parameter = JaxrsParameterExtension.getParameter(paramType, parameter, annotation);
            }

            if (parameter != null) {
                if (apiParaIdx != -1) {
                    ApiParam param = (ApiParam) accessibleObject.getAnnotations()[apiParaIdx];
                    parameter.setDescription(param.value());
                    parameter.setRequired(param.required());
                    parameter.setAccess(param.access());

                    if (parameter instanceof AbstractSerializableParameter && StringUtils.isNotEmpty(param.defaultValue())) {
                        ((AbstractSerializableParameter)parameter).setDefaultValue(param.defaultValue());
                    }

                    AllowableValues allowableValues = AllowableValuesUtils.create(param.allowableValues());
                    if (allowableValues != null) {
                        Map<PropertyBuilder.PropertyId, Object> args = allowableValues.asPropertyArguments();
                        if (args.containsKey(PropertyBuilder.PropertyId.ENUM)) {
                            parameter.setEnum((List<String>) args.get(PropertyBuilder.PropertyId.ENUM));
                        }
                    }

                    if (!param.name().isEmpty()) {
                        parameter.setName(param.name());
                    }
                }
                if (!hidden) {
                    parameters.add(parameter);
                }
            }
        }

        return parameters;
    }

    @Override
    public boolean shouldIgnoreClass(Class<?> cls) {
        return FormDataContentDisposition.class.equals(cls);
    }

    private List<AccessibleObject> getDeclaredAndInheritedFieldsAndMethods(Class<?> clazz) {
        List<AccessibleObject> accessibleObjects = new ArrayList<AccessibleObject>();
        recurseGetDeclaredAndInheritedFields(clazz, accessibleObjects);
        recurseGetDeclaredAndInheritedMethods(clazz, accessibleObjects);
        return accessibleObjects;
    }

    private void recurseGetDeclaredAndInheritedFields(Class<?> clazz, List<AccessibleObject> fields) {
        fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            recurseGetDeclaredAndInheritedFields(superClass, fields);
        }
    }

    private void recurseGetDeclaredAndInheritedMethods(Class<?> clazz, List<AccessibleObject> methods) {
        methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null) {
            recurseGetDeclaredAndInheritedMethods(superClass, methods);
        }
    }

    private Type extractType(AccessibleObject accessibleObject, Type defaulType) {
        if (accessibleObject instanceof Field) {
            return ((Field) accessibleObject).getGenericType();
        } else if (accessibleObject instanceof Method) {
            Method method = (Method) accessibleObject;
            if (method.getParameterTypes().length == 1) {
                return method.getParameterTypes()[0];
            }
        }
        return defaulType;
    }
}
