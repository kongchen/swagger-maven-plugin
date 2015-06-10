package com.github.kongchen.swagger.docgen.jaxrs;

import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.Property;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by chekong on 15/5/12.
 */
public class JaxrsParameterExtension extends AbstractSwaggerExtension implements SwaggerExtension {
    public List<Parameter> extractParameters(Annotation[] annotations, Class<?> cls, boolean isArray, Set<Class<?>> classesToSkip, Iterator<SwaggerExtension> chain) {

        if(this.shouldIgnoreClass(cls))
            return new ArrayList<Parameter>();

        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = null;
        for(Annotation annotation : annotations) {
            parameter = getParameter(cls, parameter, annotation);
        }
        if(parameter != null) {
            parameters.add(parameter);
        }

        return parameters;
    }

    public static Parameter getParameter(Class<?> cls, Parameter parameter, Annotation annotation) {
        String defaultValue = "";
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

            if (qp.getType().equals("ref") || qp.getType().equals("array")) {
                qp.setType("string");
            }
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

            if (pp.getType().equals("ref")|| pp.getType().equals("array")) {
                pp.setType("string");
            }
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

            if (hp.getType().equals("ref")|| hp.getType().equals("array")) {
                hp.setType("string");
            }
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

            if (cp.getType().equals("ref")|| cp.getType().equals("array")) {
                cp.setType("string");
            }
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

            if (fp.getType().equals("ref")|| fp.getType().equals("array")) {
                fp.setType("string");
            }
            parameter = (Parameter) fp;
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

    public boolean shouldIgnoreClass(Class<?> cls) {
        boolean output = false;
        if(cls.getName().startsWith("javax.ws.rs"))
            output = true;
        else
            output = false;
        return output;
    }
}
