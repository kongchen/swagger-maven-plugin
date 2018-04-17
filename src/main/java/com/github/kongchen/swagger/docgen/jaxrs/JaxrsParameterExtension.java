package com.github.kongchen.swagger.docgen.jaxrs;

import com.sun.jersey.multipart.FormDataParam;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.Property;

import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author chekong on 15/5/12.
 */
public class JaxrsParameterExtension extends AbstractSwaggerExtension {

    @Override
    public List<Parameter> extractParameters(final List<Annotation> annotations, final Type type, final Set<Type> typesToSkip, final Iterator<SwaggerExtension> chain) {
        if (this.shouldIgnoreType(type, typesToSkip)) {
            return new ArrayList<Parameter>();
        }

        final List<Parameter> parameters = new ArrayList<Parameter>();
        SerializableParameter parameter = null;
        for (final Annotation annotation : annotations) {
            parameter = getParameter(type, parameter, annotation);
        }
        if (parameter != null) {
            parameters.add(parameter);
        }

        return parameters;
    }

    public static SerializableParameter getParameter(final Type type, SerializableParameter parameter, final Annotation annotation) {
        String defaultValue = "";
        if (annotation instanceof DefaultValue) {
            final DefaultValue defaultValueAnnotation = (DefaultValue) annotation;
            defaultValue = defaultValueAnnotation.value();
        }

        if (annotation instanceof QueryParam) {
            final QueryParam param = (QueryParam) annotation;
            final QueryParameter queryParameter = new QueryParameter().name(param.value());

            if (!defaultValue.isEmpty()) {
                queryParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                queryParameter.setProperty(schema);
            }

            final String parameterType = queryParameter.getType();
            if (parameterType == null || parameterType.equals("ref")) {
                queryParameter.setType("string");
            }
            parameter = queryParameter;
        } else if (annotation instanceof PathParam) {
            final PathParam param = (PathParam) annotation;
            final PathParameter pathParameter = new PathParameter().name(param.value());
            if (!defaultValue.isEmpty()) {
                pathParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                pathParameter.setProperty(schema);
            }

            final String parameterType = pathParameter.getType();
            if (parameterType == null || parameterType.equals("ref")) {
                pathParameter.setType("string");
            }
            parameter = pathParameter;
        } else if (annotation instanceof HeaderParam) {
            final HeaderParam param = (HeaderParam) annotation;
            final HeaderParameter headerParameter = new HeaderParameter().name(param.value());
            headerParameter.setDefaultValue(defaultValue);
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                headerParameter.setProperty(schema);
            }

            final String parameterType = headerParameter.getType();
            if (parameterType == null || parameterType.equals("ref") || parameterType.equals("array")) {
                headerParameter.setType("string");
            }
            parameter = headerParameter;
        } else if (annotation instanceof CookieParam) {
            final CookieParam param = (CookieParam) annotation;
            final CookieParameter cookieParameter = new CookieParameter().name(param.value());
            if (!defaultValue.isEmpty()) {
                cookieParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                cookieParameter.setProperty(schema);
            }

            final String parameterType = cookieParameter.getType();
            if (parameterType == null || parameterType.equals("ref") || parameterType.equals("array")) {
                cookieParameter.setType("string");
            }
            parameter = cookieParameter;
        } else if (annotation instanceof FormParam) {
            final FormParam param = (FormParam) annotation;
            final FormParameter formParameter = new FormParameter().name(param.value());
            if (!defaultValue.isEmpty()) {
                formParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                formParameter.setProperty(schema);
            }

            final String parameterType = formParameter.getType();
            if (parameterType == null || parameterType.equals("ref") || parameterType.equals("array")) {
                formParameter.setType("string");
            }
            parameter = formParameter;
        } else if (annotation instanceof FormDataParam) {
            final FormDataParam param = (FormDataParam) annotation;
            final FormParameter formParameter = new FormParameter().name(param.value());
            if (!defaultValue.isEmpty()) {
                formParameter.setDefaultValue(defaultValue);
            }
            final Property schema = ModelConverters.getInstance().readAsProperty(type);
            if (schema != null) {
                formParameter.setProperty(schema);
            }

            final String parameterType = formParameter.getType();
            if (parameterType == null || parameterType.equals("ref") || parameterType.equals("array")) {
                formParameter.setType("string");
            }
            parameter = formParameter;
        }

        //                //fix parameter type issue, try to access parameter's type
//                try {
//                    Field t = parameter.getClass().getDeclaredField("type");
//                    t.setAccessible(true);
//                    Object tval = t.get(parameter);
//                    if (tval.equals("ref")) {
//                        //fix to string
//                        t.set(parameter, "string");
//                    }
//                    t.setAccessible(false);
//                } catch (NoSuchFieldException e) {
//                    //ignore
//                } catch (IllegalAccessException e) {
//                    //ignore
//                }
//
        return parameter;
    }
}
