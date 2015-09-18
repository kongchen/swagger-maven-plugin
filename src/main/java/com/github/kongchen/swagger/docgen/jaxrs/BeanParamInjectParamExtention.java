package com.github.kongchen.swagger.docgen.jaxrs;

import com.sun.jersey.api.core.InjectParam;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;

import org.reflections.util.Utils;

import javax.ws.rs.BeanParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.reflect.TypeUtils;

/**
 * Created by chekong on 15/5/9.
 */
public class BeanParamInjectParamExtention extends AbstractSwaggerExtension implements SwaggerExtension {

    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip, Iterator<SwaggerExtension> chain) {
        Class<?> cls = TypeUtils.getRawType(type, type);
        
        List<Parameter> output = new ArrayList<Parameter>();
        if(shouldIgnoreClass(cls) || typesToSkip.contains(type)) {
            // stop the processing chain
            typesToSkip.add(type);
            return output;
        }
        for(Annotation annotation : annotations) {
            if(annotation instanceof BeanParam || annotation instanceof InjectParam) {
                return extractParameters(cls);

            }
        }
        if(chain.hasNext())
            return chain.next().extractParameters(annotations, type, typesToSkip , chain);
        return null;
    }

    private List<Parameter> extractParameters(Class<?> cls) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        
        for (Field f : getDeclaredAndInheritedFields(cls)) {
            Parameter parameter = null;

            int i = 0, apiParaIdx = -1;

            for(Annotation annotation : f.getAnnotations()) {
                if(annotation instanceof ApiParam) {
                    apiParaIdx = i;
                }
                i++;
                parameter = JaxrsParameterExtension.getParameter(cls, parameter, annotation);

            }
            if (parameter != null) {
                if(apiParaIdx != -1) {
                    ApiParam param = (ApiParam) f.getAnnotations()[apiParaIdx];
                    parameter.setDescription(param.value());
                    parameter.setRequired(param.required());
                    parameter.setAccess(param.access());
                    if(!Utils.isEmpty(param.name())) {
                        parameter.setName(param.name());
                    }
                }
                parameters.add(parameter);
            }
        }

        return parameters;
    }

    public boolean shouldIgnoreClass(Class<?> cls) {
        boolean output = false;
        if(com.sun.jersey.core.header.FormDataContentDisposition.class.equals(cls))
            output = true;
        else
            output = false;

        return output;
    }

    private  List<Field> getDeclaredAndInheritedFields(Class<?> c) {
        List<Field> fields = new ArrayList<Field>();
          recurseGetDeclaredAndInheritedFields(c, fields);
          return fields;
    }
    
    private void recurseGetDeclaredAndInheritedFields(Class<?> c, List<Field> fields) {
        fields.addAll(Arrays.asList(c.getDeclaredFields())); 
        Class<?> superClass = c.getSuperclass(); 
        if (superClass != null) { 
            recurseGetDeclaredAndInheritedFields(superClass, fields); 
        }  
    }	
}
