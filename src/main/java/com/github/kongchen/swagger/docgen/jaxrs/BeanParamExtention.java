package com.github.kongchen.swagger.docgen.jaxrs;

import com.wordnik.swagger.annotations.ApiParam;
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
import org.reflections.util.Utils;

import javax.ws.rs.BeanParam;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by chekong on 15/5/9.
 */
public class BeanParamExtention extends AbstractSwaggerExtension implements SwaggerExtension {
    public List<Parameter> extractParameters(Annotation[] annotations, Class<?> cls, boolean isArray, Set<Class<?>> classesToSkip, Iterator<SwaggerExtension> chain) {
        List<Parameter> output = new ArrayList<Parameter>();
        if(shouldIgnoreClass(cls) || classesToSkip.contains(cls)) {
            // stop the processing chain
            classesToSkip.add(cls);
            return output;
        }
        for(Annotation annotation : annotations) {
            if(annotation instanceof BeanParam) {
                return extractParameterFromBeanParam(cls);

            }
        }
        if(chain.hasNext())
            return chain.next().extractParameters(annotations, cls, isArray, classesToSkip, chain);
        return null;
    }

    private List<Parameter> extractParameterFromBeanParam(Class<?> cls) {
        List<Parameter> parameters = new ArrayList<Parameter>();
        for (Field f : cls.getDeclaredFields()) {
            Parameter parameter = null;
            String defaultValue = "";
            int i = 0, apiParaIdx = -1;

            for(Annotation annotation : f.getAnnotations()) {
                if(annotation instanceof ApiParam) {
                    apiParaIdx = i;
                }
                i++;
                if(annotation instanceof DefaultValue) {
                    DefaultValue defaultValueAnnotation = (DefaultValue) annotation;
                    defaultValue = defaultValueAnnotation.value();
                }

                if(annotation instanceof QueryParam) {
                    QueryParam param = (QueryParam) annotation;
                    QueryParameter qp = new QueryParameter()
                            .name(param.value());

                    if(!defaultValue.isEmpty()) {
                        qp.setDefaultValue(defaultValue);
                    }
                    Property schema = ModelConverters.getInstance().readAsProperty(cls);
                    if(schema != null)
                        qp.setProperty(schema);
                    parameter = qp;
                }
                else if(annotation instanceof PathParam) {
                    PathParam param = (PathParam) annotation;
                    PathParameter pp = new PathParameter()
                            .name(param.value());
                    if(!defaultValue.isEmpty())
                        pp.setDefaultValue(defaultValue);
                    Property schema = ModelConverters.getInstance().readAsProperty(cls);
                    if(schema != null)
                        pp.setProperty(schema);
                    parameter = pp;
                }
                else if(annotation instanceof HeaderParam) {
                    HeaderParam param = (HeaderParam) annotation;
                    HeaderParameter hp = new HeaderParameter()
                            .name(param.value());
                    hp.setDefaultValue(defaultValue);
                    Property schema = ModelConverters.getInstance().readAsProperty(cls);
                    if(schema != null)
                        hp.setProperty(schema);
                    parameter = hp;
                }
                else if(annotation instanceof CookieParam) {
                    CookieParam param = (CookieParam) annotation;
                    CookieParameter cp = new CookieParameter()
                            .name(param.value());
                    if(!defaultValue.isEmpty())
                        cp.setDefaultValue(defaultValue);
                    Property schema = ModelConverters.getInstance().readAsProperty(cls);
                    if(schema != null)
                        cp.setProperty(schema);
                    parameter = cp;
                }
                else if(annotation instanceof FormParam) {
                    FormParam param = (FormParam) annotation;
                    FormParameter fp = new FormParameter()
                            .name(param.value());
                    if(!defaultValue.isEmpty())
                        fp.setDefaultValue(defaultValue);
                    Property schema = ModelConverters.getInstance().readAsProperty(cls);
                    if(schema != null)
                        fp.setProperty(schema);
                    parameter = fp;
                }

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
}
