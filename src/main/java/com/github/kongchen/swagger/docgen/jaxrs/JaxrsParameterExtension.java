package com.github.kongchen.swagger.docgen.jaxrs;

import com.google.common.base.Strings;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.*;
import io.swagger.models.properties.Property;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author chekong on 15/5/12.
 */
public class JaxrsParameterExtension extends AbstractSwaggerExtension {

    @Override
    public List<Parameter> extractParameters(List<Annotation> annotations, Type type, Set<Type> typesToSkip, Iterator<SwaggerExtension> chain) {
        if (this.shouldIgnoreType(type, typesToSkip)) {
            return new ArrayList<Parameter>();
        }

        ClassToInstanceMap<Annotation> annotationMap = toMap(annotations);

        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.addAll(extractParametersFromAnnotation(type, annotationMap));

        if (!parameters.isEmpty()) {
            return parameters;
        }
        return super.extractParameters(annotations, type, typesToSkip, chain);
    }

    private ClassToInstanceMap<Annotation> toMap(Collection<? extends Annotation> annotations) {
        ClassToInstanceMap<Annotation> annotationMap = MutableClassToInstanceMap.create();
        for (Annotation annotation : annotations) {
            if (annotation == null) {
                continue;
            }
            annotationMap.put(annotation.annotationType(), annotation);
        }

        return annotationMap;
    }

    private List<Parameter> extractParametersFromAnnotation(Type type, ClassToInstanceMap<Annotation> annotations) {
        String defaultValue = "";
        if (annotations.containsKey(DefaultValue.class)) {
            DefaultValue defaultValueAnnotation = annotations.getInstance(DefaultValue.class);
            defaultValue = defaultValueAnnotation.value();
        }

        List<Parameter> parameters = new ArrayList<>();
        if (annotations.containsKey(QueryParam.class)) {
            QueryParam param = annotations.getInstance(QueryParam.class);
            QueryParameter queryParameter = extractQueryParam(type, defaultValue, param);
            parameters.add(queryParameter);
        } else if (annotations.containsKey(PathParam.class)) {
            PathParam param = annotations.getInstance(PathParam.class);
            PathParameter pathParameter = extractPathParam(type, defaultValue, param);
            parameters.add(pathParameter);
        } else if (annotations.containsKey(HeaderParam.class)) {
            HeaderParam param = annotations.getInstance(HeaderParam.class);
            HeaderParameter headerParameter = extractHeaderParam(type, defaultValue, param);
            parameters.add(headerParameter);
        } else if (annotations.containsKey(CookieParam.class)) {
            CookieParam param = annotations.getInstance(CookieParam.class);
            CookieParameter cookieParameter = extractCookieParameter(type, defaultValue, param);
            parameters.add(cookieParameter);
        } else if (annotations.containsKey(FormParam.class)) {
            FormParam param = annotations.getInstance(FormParam.class);
            FormParameter formParameter = extractFormParameter(type, defaultValue, param);
            parameters.add(formParameter);
        }

        return parameters;
    }

    private QueryParameter extractQueryParam(Type type, String defaultValue, QueryParam param) {
        QueryParameter queryParameter = new QueryParameter().name(param.value());

        if (!Strings.isNullOrEmpty(defaultValue)) {
            queryParameter.setDefaultValue(defaultValue);
        }
        Property schema = ModelConverters.getInstance().readAsProperty(type);
        if (schema != null) {
            queryParameter.setProperty(schema);
        }

        String parameterType = queryParameter.getType();

        if (parameterType == null || parameterType.equals("ref")) {
            queryParameter.setType("string");
        }
        return queryParameter;
    }

    private PathParameter extractPathParam(Type type, String defaultValue, PathParam param) {
        PathParameter pathParameter = new PathParameter().name(param.value());
        if (!Strings.isNullOrEmpty(defaultValue)) {
            pathParameter.setDefaultValue(defaultValue);
        }
        Property schema = ModelConverters.getInstance().readAsProperty(type);
        if (schema != null) {
            pathParameter.setProperty(schema);
        }

        String parameterType = pathParameter.getType();
        if (parameterType == null || parameterType.equals("ref")) {
            pathParameter.setType("string");
        }
        return pathParameter;
    }

    private HeaderParameter extractHeaderParam(Type type, String defaultValue, HeaderParam param) {
        HeaderParameter headerParameter = new HeaderParameter().name(param.value());
        if (!Strings.isNullOrEmpty(defaultValue)) {
            headerParameter.setDefaultValue(defaultValue);
        }
        Property schema = ModelConverters.getInstance().readAsProperty(type);
        if (schema != null) {
            headerParameter.setProperty(schema);
        }

        String parameterType = headerParameter.getType();
        if (parameterType == null || parameterType.equals("ref") || parameterType.equals("array")) {
            headerParameter.setType("string");
        }
        return headerParameter;
    }

    private CookieParameter extractCookieParameter(Type type, String defaultValue, CookieParam param) {
        CookieParameter cookieParameter = new CookieParameter().name(param.value());
        if (!Strings.isNullOrEmpty(defaultValue)) {
            cookieParameter.setDefaultValue(defaultValue);
        }
        Property schema = ModelConverters.getInstance().readAsProperty(type);
        if (schema != null) {
            cookieParameter.setProperty(schema);
        }

        String parameterType = cookieParameter.getType();
        if (parameterType == null || parameterType.equals("ref") || parameterType.equals("array")) {
            cookieParameter.setType("string");
        }
        return cookieParameter;
    }

    private FormParameter extractFormParameter(Type type, String defaultValue, FormParam param) {
        FormParameter formParameter = new FormParameter().name(param.value());
        if (!Strings.isNullOrEmpty(defaultValue)) {
            formParameter.setDefaultValue(defaultValue);
        }
        Property schema = ModelConverters.getInstance().readAsProperty(type);
        if (schema != null) {
            formParameter.setProperty(schema);
        }

        String parameterType = formParameter.getType();
        if (parameterType == null || parameterType.equals("ref") || parameterType.equals("array")) {
            formParameter.setType("string");
        }
        return formParameter;
    }
}
