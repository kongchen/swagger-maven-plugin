package com.github.kongchen.swagger.docgen.spring;

import com.fasterxml.jackson.databind.JavaType;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiParam;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.maven.plugin.logging.Log;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author chekong on 15/4/27.
 */
public class SpringSwaggerExtension extends AbstractSwaggerExtension {

    private final static String DEFAULT_VALUE = "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n";

    private static final RequestParam DEFAULT_REQUEST_PARAM = (RequestParam)MethodUtils.getMatchingMethod(AnnotationBearer.class, "get", String.class).getParameterAnnotations()[0][0];

    private Log log;

    // Class specificly for holding default value annotations
    private static class AnnotationBearer {
        public void get(@RequestParam String requestParam) {
        }
    }

    public SpringSwaggerExtension(Log log) {
        this.log = log;
    }

    @Override
    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip, Iterator<SwaggerExtension> chain) {
        if (this.shouldIgnoreType(type, typesToSkip)) {
            return new ArrayList<Parameter>();
        }

        if (annotations.isEmpty()) {
            // Method arguments are not required to have any annotations
            annotations = Lists.newArrayList((Annotation) null);
        }

        Map<Class<?>, Annotation> annotationMap = toMap(annotations);

        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.addAll(extractParametersFromModelAttributeAnnotation(type, annotationMap));
        parameters.addAll(extractParameterFromAnnotation(type, annotationMap));

        if (!parameters.isEmpty()) {
            return parameters;
        }
        return super.extractParameters(annotations, type, typesToSkip, chain);
    }

    private Map<Class<?>, Annotation> toMap(Collection<? extends Annotation> annotations) {
        Map<Class<?>, Annotation> annotationMap = new HashMap<>();
        for (Annotation annotation : annotations) {
            annotationMap.put(annotation.annotationType(), annotation);
        }

        return annotationMap;
    }

    private boolean hasClassStartingWith(Collection<Class<?>> list, String value) {
        for (Class<?> aClass : list) {
            if (aClass.getName().startsWith(value)) {
                return true;
            }
        }

        return false;
    }

    private List<Parameter> extractParameterFromAnnotation(Type type, Map<Class<?>, Annotation> annotations) {
        List<Parameter> parameters = new ArrayList<>();

        if (isRequestParamType(type, annotations)) {
            parameters.add(extractParameterFromRequestParam(type, (RequestParam)annotations.get(RequestParam.class)));
        }
        if (annotations.containsKey(PathVariable.class)) {
            PathVariable pathVariable = (PathVariable) annotations.get(PathVariable.class);
            String paramName = StringUtils.defaultIfEmpty(pathVariable.value(), pathVariable.name());
            PathParameter pathParameter = new PathParameter().name(paramName);
            Property schema = readAsPropertyIfPrimitive(type);
            if (schema != null) {
                pathParameter.setProperty(schema);
            }
            parameters.add(pathParameter);
        }
        if (annotations.containsKey(RequestHeader.class)) {
            RequestHeader requestHeader = (RequestHeader) annotations.get(RequestHeader.class);
            String paramName = StringUtils.defaultIfEmpty(requestHeader.value(), requestHeader.name());
            HeaderParameter headerParameter = new HeaderParameter().name(paramName)
                    .required(requestHeader.required());
            Property schema = readAsPropertyIfPrimitive(type);
            if (!DEFAULT_VALUE.equals(requestHeader.defaultValue())) {
                headerParameter.setDefaultValue(requestHeader.defaultValue());
                headerParameter.setRequired(false);
            }
            if (schema != null) {
                headerParameter.setProperty(schema);
            }

            parameters.add(headerParameter);
        }
        if (annotations.containsKey(CookieValue.class)) {
            CookieValue cookieValue = (CookieValue) annotations.get(CookieValue.class);
            String paramName = StringUtils.defaultIfEmpty(cookieValue.value(), cookieValue.name());
            CookieParameter cookieParameter = new CookieParameter().name(paramName)
                    .required(cookieValue.required());
            Property schema = readAsPropertyIfPrimitive(type);
            if (!DEFAULT_VALUE.equals(cookieValue.defaultValue())) {
                cookieParameter.setDefaultValue(cookieValue.defaultValue());
                cookieParameter.setRequired(false);
            }
            if (schema != null) {
                cookieParameter.setProperty(schema);
            }

            parameters.add(cookieParameter);
        }
        if (annotations.containsKey(RequestPart.class)) {
            RequestPart requestPart = (RequestPart) annotations.get(RequestPart.class);
            String paramName = StringUtils.defaultIfEmpty(requestPart.value(), requestPart.name());
            FormParameter formParameter = new FormParameter().name(paramName)
                    .required(requestPart.required());

            JavaType ct = constructType(type);
            Property schema;

            if (MultipartFile.class.isAssignableFrom(ct.getRawClass())) {
                schema = new FileProperty();
            } else if (ct.isContainerType() &&
                    MultipartFile.class.isAssignableFrom(ct.getContentType().getRawClass())) {
                schema = new ArrayProperty().items(new FileProperty());
            } else {
                schema = ModelConverters.getInstance().readAsProperty(type);
            }

            if (schema != null) {
                formParameter.setProperty(schema);
            }

            parameters.add(formParameter);
        }

        return parameters;
    }

    private Property readAsPropertyIfPrimitive(Type type) {
        if (com.github.kongchen.swagger.docgen.util.TypeUtils.isPrimitive(type)) {
            return ModelConverters.getInstance().readAsProperty(type);
        } else {
            String msg = String.format("Can't use non-primitive type: %s as request parameter", type);
            log.error(msg);

            // fallback to string if type is simple wrapper for String to support legacy code
            JavaType ct = constructType(type);
            if (isSimpleWrapperForString(ct.getRawClass())) {
                log.warn(String.format("Non-primitive type: %s used as string for request parameter", type));
                return new StringProperty();
            }
        }
        return null;
    }

    private boolean isSimpleWrapperForString(Class<?> clazz) {
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            if (constructors.length <= 2) {
                if (constructors.length == 1) {
                    return clazz.getConstructor(String.class) != null;
                } else if (constructors.length == 2) {
                    return clazz.getConstructor(String.class) != null && clazz.getConstructor() != null;
                }
            }
            return false;
        } catch (NoSuchMethodException e) {
            // ignore
            return false;
        }
    }

    private List<Parameter> extractParametersFromModelAttributeAnnotation(Type type, Map<Class<?>, Annotation> annotations) {
        ModelAttribute modelAttribute = (ModelAttribute)annotations.get(ModelAttribute.class);
        if ((modelAttribute == null || !hasClassStartingWith(annotations.keySet(), "org.springframework.web.bind.annotation"))&& BeanUtils.isSimpleProperty(TypeUtils.getRawType(type, null))) {
            return Collections.emptyList();
        }

        List<Parameter> parameters = new ArrayList<Parameter>();
        Class<?> clazz = TypeUtils.getRawType(type, type);
        for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(clazz)) {
            // Get all the valid setter methods inside the bean
            Method propertyDescriptorSetter = propertyDescriptor.getWriteMethod();
            if (propertyDescriptorSetter != null) {
                ApiParam propertySetterApiParam = AnnotationUtils.findAnnotation(propertyDescriptorSetter, ApiParam.class);
                if (propertySetterApiParam == null) {
                    // If we find a setter that doesn't have @ApiParam annotation, then skip it
                    continue;
                }

                // Here we have a bean setter method that is annotted with @ApiParam, but we still
                // need to know what type of parameter to create. In order to do this, we look for
                // any annotation attached to the first method parameter of the setter fucntion.
                Annotation[][] methodAnnotations = propertyDescriptorSetter.getParameterAnnotations();
                if (methodAnnotations == null || methodAnnotations.length == 0) {
                    continue;
                }

                Class parameterClass = propertyDescriptor.getPropertyType();
                List<Parameter> propertySetterExtractedParameters = this.extractParameterFromAnnotation(
                        parameterClass, toMap(Arrays.asList(methodAnnotations[0])));

                if (propertySetterExtractedParameters.isEmpty()) {
                    QueryParameter queryParameter = new QueryParameter().name(propertyDescriptor.getDisplayName())
                            .description(propertySetterApiParam.value())
                            .required(propertySetterApiParam.required());
                    queryParameter.setAccess(propertySetterApiParam.access());
                    Property schema = ModelConverters.getInstance()
                            .readAsProperty(propertyDescriptor.getPropertyType());
                    if (schema != null) {
                        queryParameter.setProperty(schema);
                    }
                    if (!propertySetterApiParam.name().isEmpty()) {
                        queryParameter.setName(propertySetterApiParam.name());
                    }
                    parameters.add(queryParameter);
                } else {
                    parameters.add(propertySetterExtractedParameters.get(0));
                }
            }
        }

        return parameters;
    }

    private boolean isRequestParamType(Type type, Map<Class<?>, Annotation> annotations) {
        RequestParam requestParam = (RequestParam)annotations.get(RequestParam.class);
        return requestParam != null || (!hasClassStartingWith(annotations.keySet(), "org.springframework.web.bind.annotation")) && BeanUtils.isSimpleProperty(TypeUtils.getRawType(type, type));
    }

    private Parameter extractParameterFromRequestParam(Type type, RequestParam requestParam) {
        if (requestParam == null) {
            requestParam = DEFAULT_REQUEST_PARAM;
        }

        String paramName = StringUtils.defaultIfEmpty(requestParam.value(), requestParam.name());
        QueryParameter queryParameter = new QueryParameter().name(paramName)
                .required(requestParam.required());

        if (!DEFAULT_VALUE.equals(requestParam.defaultValue())) {
            queryParameter.setDefaultValue(requestParam.defaultValue());
            // Supplying a default value implicitly sets required() to false.
            queryParameter.setRequired(false);
        }
        Property schema = readAsPropertyIfPrimitive(type);
        if (schema != null) {
            queryParameter.setProperty(schema);
        }

        return queryParameter;
    }

    @Override
    public boolean shouldIgnoreType(Type type, Set<Type> typesToSkip) {
        Class<?> cls = TypeUtils.getRawType(type, type);
        if (cls == null) {
            return false;
        }

        // TODO: Some classes are missing in this ignore list. compare with
        // https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-arguments

        return cls.getName().startsWith("org.springframework") &&
                !cls.getName().equals("org.springframework.web.multipart.MultipartFile");
    }
}
