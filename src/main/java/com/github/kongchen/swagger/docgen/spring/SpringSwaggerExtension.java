package com.github.kongchen.swagger.docgen.spring;

import com.fasterxml.jackson.databind.JavaType;
import io.swagger.annotations.ApiParam;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.FileProperty;
import io.swagger.models.properties.Property;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
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

    @Override
    public List<Parameter> extractParameters(final List<Annotation> annotations, final Type type, final Set<Type> typesToSkip, final Iterator<SwaggerExtension> chain) {
        if (this.shouldIgnoreType(type, typesToSkip)) {
            return new ArrayList<Parameter>();
        }

        final String defaultValue = "";
        final List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = null;
        for (final Annotation annotation : annotations) {
            if (annotation instanceof ModelAttribute) {
                parameters.addAll(extractParametersFromModelAttributeAnnotation(annotation, type));
            } else {
                parameter = extractParameterFromAnnotation(annotation, defaultValue, type);
            }

            if (parameter != null) {
                parameters.add(parameter);
            }
        }

        return parameters;
    }

    private Parameter extractParameterFromAnnotation(final Annotation annotation, final String defaultValue, final Type type) {
        Parameter parameter = null;

        if (annotation instanceof RequestParam) {
            final RequestParam requestParam = (RequestParam) annotation;
            final String paramName = StringUtils.defaultIfEmpty(requestParam.value(), requestParam.name());
            final QueryParameter queryParameter = new QueryParameter().name(paramName)
                    .required(requestParam.required());

            if (!defaultValue.isEmpty()) {
                queryParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                queryParameter.setProperty(schema);
            }

            parameter = queryParameter;
        } else if (annotation instanceof PathVariable) {
            final PathVariable pathVariable = (PathVariable) annotation;
            final String paramName = StringUtils.defaultIfEmpty(pathVariable.value(), pathVariable.name());
            final PathParameter pathParameter = new PathParameter().name(paramName);
            if (!defaultValue.isEmpty()) {
                pathParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                pathParameter.setProperty(schema);
            }
            parameter = pathParameter;
        } else if (annotation instanceof RequestHeader) {
            final RequestHeader requestHeader = (RequestHeader) annotation;
            final String paramName = StringUtils.defaultIfEmpty(requestHeader.value(), requestHeader.name());
            final HeaderParameter headerParameter = new HeaderParameter().name(paramName)
                    .required(requestHeader.required());
            headerParameter.setDefaultValue(defaultValue);
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                headerParameter.setProperty(schema);
            }

            parameter = headerParameter;
        } else if (annotation instanceof CookieValue) {
            final CookieValue cookieValue = (CookieValue) annotation;
            final String paramName = StringUtils.defaultIfEmpty(cookieValue.value(), cookieValue.name());
            final CookieParameter cookieParameter = new CookieParameter().name(paramName)
                    .required(cookieValue.required());
            if (!defaultValue.isEmpty()) {
                cookieParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                cookieParameter.setProperty(schema);
            }

            parameter = cookieParameter;
        } else if (annotation instanceof RequestPart) {
            final RequestPart requestPart = (RequestPart) annotation;
            final String paramName = StringUtils.defaultIfEmpty(requestPart.value(), requestPart.name());
            final FormParameter formParameter = new FormParameter().name(paramName)
                    .required(requestPart.required());

            if (!defaultValue.isEmpty()) {
                formParameter.setDefaultValue(defaultValue);
            }

            final JavaType ct = constructType(type);
            final Property schema;

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

    private List<Parameter> extractParametersFromModelAttributeAnnotation(final Annotation annotation, final Type type) {
        if (!(annotation instanceof ModelAttribute)) {
            return null;
        }

        final Class<?> cls = TypeUtils.getRawType(type, type);

        final List<Parameter> parameters = new ArrayList<Parameter>();
        for (final PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(cls)) {
            // Get all the valid setter methods inside the bean
            final Method propertyDescriptorSetter = propertyDescriptor.getWriteMethod();
            if (propertyDescriptorSetter != null) {
                final ApiParam propertySetterApiParam = AnnotationUtils.findAnnotation(propertyDescriptorSetter, ApiParam.class);
                if (propertySetterApiParam == null) {
                    // If we find a setter that doesn't have @ApiParam annotation, then skip it
                    continue;
                }

                // Here we have a bean setter method that is annotted with @ApiParam, but we still
                // need to know what type of parameter to create. In order to do this, we look for
                // any annotation attached to the first method parameter of the setter fucntion.
                final Annotation[][] methodAnnotations = propertyDescriptorSetter.getParameterAnnotations();
                if (methodAnnotations == null || methodAnnotations.length == 0) {
                    continue;
                }

                final String defaultValue = "";
                Parameter propertySetterExtractedParameter = null;
                for (final Annotation firstMethodParameterAnnotation : methodAnnotations[0]) {
                    final Class parameterClass = propertyDescriptor.getPropertyType();
                    propertySetterExtractedParameter = this.extractParameterFromAnnotation(
                            firstMethodParameterAnnotation, defaultValue, parameterClass);
                    if (propertySetterExtractedParameter != null) {
                        // When we find a valid parameter type to use, keep it
                        break;
                    }
                }

                if (propertySetterExtractedParameter == null) {
                    final QueryParameter queryParameter = new QueryParameter().name(propertyDescriptor.getDisplayName())
                            .description(propertySetterApiParam.value())
                            .required(propertySetterApiParam.required());
                    queryParameter.setAccess(propertySetterApiParam.access());
                    final Property schema = ModelConverters.getInstance()
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
    public boolean shouldIgnoreType(final Type type, final Set<Type> typesToSkip) {
        final Class<?> cls = TypeUtils.getRawType(type, type);
        return cls != null && cls.getName().startsWith("org.springframework") &&
                !cls.getName().equals("org.springframework.web.multipart.MultipartFile");
    }
}
