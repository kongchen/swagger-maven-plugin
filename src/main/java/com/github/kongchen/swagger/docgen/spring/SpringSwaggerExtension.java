package com.github.kongchen.swagger.docgen.spring;

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
import io.swagger.models.properties.RefProperty;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.reflections.util.Utils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JavaType;

/**
 * Created by chekong on 15/4/27.
 */
public class SpringSwaggerExtension extends AbstractSwaggerExtension implements SwaggerExtension {

    @Override
    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip, Iterator<SwaggerExtension> chain) {
        String defaultValue = "";

        if (this.shouldIgnoreType(type, typesToSkip)) {
            return new ArrayList<Parameter>();
        }

        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = null;
        for (Annotation annotation : annotations) {
            if (annotation instanceof ModelAttribute) {
                parameters.addAll(this.extractParametersFromModelAttributeAnnotation(annotation, type));
            } else {
                parameter = this.extractParameterFromAnnotation(annotation, defaultValue, type);
            }

            if (parameter != null) {
                parameters.add(parameter);
            }
        }

        return parameters;
    }

    private Parameter extractParameterFromAnnotation(Annotation annotation, String defaultValue, Type type) {
        Parameter parameter = null;

        if (annotation instanceof RequestParam) {
            RequestParam param = (RequestParam) annotation;
            QueryParameter qp = new QueryParameter()
                .name(param.value());

            if (!defaultValue.isEmpty()) {
                qp.setDefaultValue(defaultValue);
            }
            Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                qp.setProperty(schema);
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
            Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                pp.setProperty(schema);
            }
            parameter = pp;
        } else if (annotation instanceof RequestHeader) {
            RequestHeader param = (RequestHeader) annotation;
            HeaderParameter hp = new HeaderParameter()
                .name(param.value());
            hp.setDefaultValue(defaultValue);
            Property schema = ModelConverters.getInstance().readAsProperty(type);
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
            Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                cp.setProperty(schema);
            }

            cp.setRequired(param.required());

            parameter = cp;
        } else if(annotation instanceof RequestPart){
			RequestPart param = (RequestPart) annotation;
			FormParameter fp = new FormParameter()
					.name(param.value());

			if (!defaultValue.isEmpty()) {
				fp.setDefaultValue(defaultValue);
			}
			
			JavaType ct = constructType(type);
			Property schema;
			
			if(MultipartFile.class.isAssignableFrom(ct.getRawClass())){
				schema = new FileProperty();
			} else if(ct.isContainerType() && 
					MultipartFile.class.isAssignableFrom(ct.getContentType().getRawClass())){
				ArrayProperty arrayProperty = new ArrayProperty();
				arrayProperty.setItems(new FileProperty());
				schema = arrayProperty;
			} else{
				schema = ModelConverters.getInstance().readAsProperty(type);
			}

			if (schema != null) {
				fp.setProperty(schema);
			}
			
			fp.setRequired(param.required());

			parameter = fp;	
		}

        return parameter;
    }

    private List<Parameter> extractParametersFromModelAttributeAnnotation(Annotation annotation, Type type) {
        if (false == (annotation instanceof ModelAttribute)) {
            return null;
        }
        
        Class<?> cls = TypeUtils.getRawType(type, type);
        
        List<Parameter> parameters = new ArrayList<Parameter>();
        // If ModelAttribute annotation is present, check for possible APIparam annotation in beans
        for (PropertyDescriptor propertyDescriptor : BeanUtils.getPropertyDescriptors(cls)) {

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
                    propertySetterExtractedParameter = this.extractParameterFromAnnotation(
                        firstMethodParameterAnnotation, defaultValue, parameterClass);
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
    public boolean shouldIgnoreType(Type type, Set<Type> typesToSkip) {
        boolean output = false;
        Class<?> cls = TypeUtils.getRawType(type, type);
        if (cls.getName().startsWith("org.springframework") &&
        		!cls.getName().equals("org.springframework.web.multipart.MultipartFile")) {
            output = true;
        } else {
            output = false;
        }
        return output;
    }
}
