package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.reader.AbstractReader;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.jaxrs.ext.AbstractSwaggerExtension;
import com.wordnik.swagger.jaxrs.ext.SwaggerExtension;
import com.wordnik.swagger.models.parameters.CookieParameter;
import com.wordnik.swagger.models.parameters.FormParameter;
import com.wordnik.swagger.models.parameters.HeaderParameter;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.parameters.QueryParameter;
import com.wordnik.swagger.models.properties.Property;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.reflections.util.Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.beans.PropertyDescriptor;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.jaxrs.utils.ParameterUtils;
import java.lang.reflect.Type;

/**
 * Created by chekong on 15/4/27.
 */
public class SpringSwaggerExtension extends AbstractSwaggerExtension implements SwaggerExtension {

    @Override
    public List<Parameter> extractParameters(Annotation[] annotations, Class<?> cls, boolean isArray, Set<Class<?>> classesToSkip, Iterator<SwaggerExtension> chain) {
        String defaultValue = "";

        if (this.shouldIgnoreClass(cls)) {
            return new ArrayList<Parameter>();
        }

        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof ModelAttribute) {
                parameters.addAll(this.extractParametersFromModelAttributeAnnotation(annotation, cls));
            } else {
                parameter = this.extractParameterFromAnnotation(annotation, defaultValue, cls, isArray);
            }
        }
        if (parameter != null) {
            parameters.add(parameter);
        }

        return parameters;
    }

    private Parameter extractParameterFromAnnotation(Annotation annotation, String defaultValue, Class<?> cls, boolean isArray) {
        Parameter parameter = null;

        if (annotation instanceof RequestParam) {
            RequestParam param = (RequestParam) annotation;
            QueryParameter qp = new QueryParameter()
                .name(param.value());

            if (!defaultValue.isEmpty()) {
                qp.setDefaultValue(defaultValue);
            }
            Property schema = ModelConverters.getInstance().readAsProperty(cls);
            if (schema != null) {
                qp.setProperty(schema);
            }

            if (isArray || cls.isArray() || cls.isAssignableFrom(Collection.class)) {
                qp.setType("string");
            }

            qp.setRequired(param.required());

            parameter = qp;
        } else if (annotation instanceof PathVariable) {
            PathVariable param = (PathVariable) annotation;
            PathParameter pp = new PathParameter()
                .name(param.value());
            if (!defaultValue.isEmpty()) {
                pp.setDefaultValue(defaultValue);
            }
            Property schema = ModelConverters.getInstance().readAsProperty(cls);
            if (schema != null) {
                pp.setProperty(schema);
            }
            parameter = pp;
        } else if (annotation instanceof RequestHeader) {
            RequestHeader param = (RequestHeader) annotation;
            HeaderParameter hp = new HeaderParameter()
                .name(param.value());
            hp.setDefaultValue(defaultValue);
            Property schema = ModelConverters.getInstance().readAsProperty(cls);
            if (schema != null) {
                hp.setProperty(schema);
            }

            hp.setRequired(param.required());

            parameter = hp;
        } else if (annotation instanceof CookieValue) {
            CookieValue param = (CookieValue) annotation;
            CookieParameter cp = new CookieParameter()
                .name(param.value());
            if (!defaultValue.isEmpty()) {
                cp.setDefaultValue(defaultValue);
            }
            Property schema = ModelConverters.getInstance().readAsProperty(cls);
            if (schema != null) {
                cp.setProperty(schema);
            }

            cp.setRequired(param.required());

            parameter = cp;
        }

        return parameter;
    }

    private List<Parameter> extractParametersFromModelAttributeAnnotation(Annotation annotation, Class beanClass) {
        if (false == (annotation instanceof ModelAttribute)) {
            return null;
        }

        List<Parameter> parameters = new ArrayList<Parameter>();
        // If ModelAttribute annotation is present, check for possible APIparam annotation in beans
        for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(beanClass)) {

            // Get all the valid setter methods inside the bean
            Method propertyDescriptorSetter = propertyDescriptor.getWriteMethod();
            if (propertyDescriptorSetter != null) {

                Annotation propertySetterApiParam = AnnotationUtils.findAnnotation(propertyDescriptorSetter, ApiParam.class);
                if (false == (propertySetterApiParam instanceof ApiParam)) {
                    // If we find a setter that doesn't have @ApiParam annotation, then skip it
                    continue;
                }

                // Here we have a bean setter method that is annotted with @ApiParam, but we still
                // need to know what type of parameter to create. In order to do this, we look for
                // any annotation attached to the first method parameter of the setter fucntion.
                Parameter propertySetterExtractedParameter = null;
                Annotation[][] methodAnnotations = propertyDescriptorSetter.getParameterAnnotations();
                if (methodAnnotations == null || methodAnnotations.length == 0) {
                    continue;
                }

                String defaultValue = "";
                for (Annotation firstMethodParameterAnnotation : methodAnnotations[0]) {
                    Class parameterClass = propertyDescriptor.getPropertyType();
                    Type type = propertyDescriptorSetter.getGenericParameterTypes()[0];
                    boolean isArray = ParameterUtils.isMethodArgumentAnArray(parameterClass, type);
                    propertySetterExtractedParameter = this.extractParameterFromAnnotation(
                        firstMethodParameterAnnotation, defaultValue, parameterClass, isArray);
                    if (propertySetterExtractedParameter instanceof Parameter) {
                        // When we find a valid parameter type to use, keep it
                        break;
                    }
                }

                if (false == (propertySetterExtractedParameter instanceof Parameter)) {
                    QueryParameter qp = new QueryParameter().name(propertyDescriptor.getDisplayName());
                    Property schema = ModelConverters.getInstance().readAsProperty(propertyDescriptor.getPropertyType());
                    if (schema != null) {
                        qp.setProperty(schema);
                    }
                    // Copy the attributes from the @ApiParam annotation into the new QueryParam
                    ApiParam methodApiParamAnnotation = (ApiParam) propertySetterApiParam;
                    qp.setDescription(methodApiParamAnnotation.value());
                    qp.setRequired(methodApiParamAnnotation.required());
                    qp.setAccess(methodApiParamAnnotation.access());
                    if (!Utils.isEmpty(methodApiParamAnnotation.name())) {
                        qp.setName(methodApiParamAnnotation.name());
                    }
                    parameters.add(qp);
                } else {
                    parameters.add(propertySetterExtractedParameter);
                }
            }
        }

        return parameters;
    }

    @Override
    public boolean shouldIgnoreClass(Class<?> cls) {
        boolean output = false;
        if (cls.getName().startsWith("org.springframework")) {
            output = true;
        } else {
            output = false;
        }
        return output;
    }
}
