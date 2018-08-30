package com.github.kongchen.swagger.docgen.spring;

import com.fasterxml.jackson.databind.JavaType;

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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author chekong on 15/4/27.
 */
public class SpringSwaggerExtension extends AbstractSwaggerExtension {

    private Log log;
    
    public SpringSwaggerExtension(Log log) {
        this.log = log;
    }

    @Override
    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip, Iterator<SwaggerExtension> chain) {
        if (this.shouldIgnoreType(type, typesToSkip)) {
            return new ArrayList<Parameter>();
        }

        String defaultValue = "";
        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof ModelAttribute) {
                parameters.addAll(extractParametersFromModelAttributeAnnotation(annotation, type));
            } else {
                parameter = extractParameterFromAnnotation(annotation, defaultValue, type);
            }

            if (parameter != null) {
                parameters.add(parameter);
            }
        }

        if (!parameters.isEmpty()) {
            return parameters;
        }
        return super.extractParameters(annotations, type, typesToSkip, chain);
    }

    private Parameter extractParameterFromAnnotation(Annotation annotation, String defaultValue, Type type) {
        Parameter parameter = null;

        if (annotation instanceof RequestParam) {
            RequestParam requestParam = (RequestParam) annotation;
            String paramName = StringUtils.defaultIfEmpty(requestParam.value(), requestParam.name());
            QueryParameter queryParameter = new QueryParameter().name(paramName)
                    .required(requestParam.required());

            if (!defaultValue.isEmpty()) {
                queryParameter.setDefaultValue(defaultValue);
            }
            
            Property schema = readAsPropertyIfPrimitive(type);
            if (schema != null) {
                queryParameter.setProperty(schema);
            }

            parameter = queryParameter;
        } else if (annotation instanceof PathVariable) {
            PathVariable pathVariable = (PathVariable) annotation;
            String paramName = StringUtils.defaultIfEmpty(pathVariable.value(), pathVariable.name());
            PathParameter pathParameter = new PathParameter().name(paramName);
            if (!defaultValue.isEmpty()) {
                pathParameter.setDefaultValue(defaultValue);
            }
            Property schema = readAsPropertyIfPrimitive(type);
            if (schema != null) {
                pathParameter.setProperty(schema);
            }
            parameter = pathParameter;
        } else if (annotation instanceof RequestHeader) {
            RequestHeader requestHeader = (RequestHeader) annotation;
            String paramName = StringUtils.defaultIfEmpty(requestHeader.value(), requestHeader.name());
            HeaderParameter headerParameter = new HeaderParameter().name(paramName)
                    .required(requestHeader.required());
            headerParameter.setDefaultValue(defaultValue);
            Property schema = readAsPropertyIfPrimitive(type);
            if (schema != null) {
                headerParameter.setProperty(schema);
            }

            parameter = headerParameter;
        } else if (annotation instanceof CookieValue) {
            CookieValue cookieValue = (CookieValue) annotation;
            String paramName = StringUtils.defaultIfEmpty(cookieValue.value(), cookieValue.name());
            CookieParameter cookieParameter = new CookieParameter().name(paramName)
                    .required(cookieValue.required());
            if (!defaultValue.isEmpty()) {
                cookieParameter.setDefaultValue(defaultValue);
            }
            Property schema = readAsPropertyIfPrimitive(type);
            if (schema != null) {
                cookieParameter.setProperty(schema);
            }

            parameter = cookieParameter;
        } else if (annotation instanceof RequestPart) {
            RequestPart requestPart = (RequestPart) annotation;
            String paramName = StringUtils.defaultIfEmpty(requestPart.value(), requestPart.name());
            FormParameter formParameter = new FormParameter().name(paramName)
                    .required(requestPart.required());

            if (!defaultValue.isEmpty()) {
                formParameter.setDefaultValue(defaultValue);
            }

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

            parameter = formParameter;
        }

        return parameter;
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
    
    private List<Parameter> extractParametersFromModelAttributeAnnotation(Annotation annotation, Type type) {
        if (!(annotation instanceof ModelAttribute)) {
            return null;
        }

        Class<?> cls = TypeUtils.getRawType(type, type);

        List<Parameter> parameters = new ArrayList<Parameter>();
        for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(cls)) {
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

                String defaultValue = "";
                Parameter propertySetterExtractedParameter = null;
                for (Annotation firstMethodParameterAnnotation : methodAnnotations[0]) {
                    Class parameterClass = propertyDescriptor.getPropertyType();
                    propertySetterExtractedParameter = this.extractParameterFromAnnotation(
                            firstMethodParameterAnnotation, defaultValue, parameterClass);
                    if (propertySetterExtractedParameter != null) {
                        // When we find a valid parameter type to use, keep it
                        break;
                    }
                }

                if (propertySetterExtractedParameter == null) {
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
                    parameters.add(propertySetterExtractedParameter);
                }
            }
        }

        return parameters;
    }

    @Override
    public boolean shouldIgnoreType(Type type, Set<Type> typesToSkip) {
        Class<?> cls = TypeUtils.getRawType(type, type);
        return cls != null && cls.getName().startsWith("org.springframework") &&
                !cls.getName().equals("org.springframework.web.multipart.MultipartFile");
    }
}
