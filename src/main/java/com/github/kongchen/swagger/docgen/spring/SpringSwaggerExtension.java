package com.github.kongchen.swagger.docgen.spring;

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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by chekong on 15/4/27.
 */
public class SpringSwaggerExtension extends AbstractSwaggerExtension implements SwaggerExtension {

    @Override
    public List<Parameter> extractParameters(Annotation[] annotations, Class<?> cls, boolean isArray, Set<Class<?>> classesToSkip, Iterator<SwaggerExtension> chain) {
        String defaultValue = "";

        if(this.shouldIgnoreClass(cls))
            return new ArrayList<Parameter>();

        List<Parameter> parameters = new ArrayList<Parameter>();
        Parameter parameter = null;
        for(Annotation annotation : annotations) {

            if(annotation instanceof RequestParam) {
                RequestParam param = (RequestParam) annotation;
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
            else if(annotation instanceof PathVariable) {
                PathVariable param = (PathVariable) annotation;
                PathParameter pp = new PathParameter()
                        .name(param.value());
                if(!defaultValue.isEmpty())
                    pp.setDefaultValue(defaultValue);
                Property schema = ModelConverters.getInstance().readAsProperty(cls);
                if(schema != null)
                    pp.setProperty(schema);
                parameter = pp;
            }
            else if(annotation instanceof RequestHeader) {
                RequestHeader param = (RequestHeader) annotation;
                HeaderParameter hp = new HeaderParameter()
                        .name(param.value());
                hp.setDefaultValue(defaultValue);
                Property schema = ModelConverters.getInstance().readAsProperty(cls);
                if(schema != null)
                    hp.setProperty(schema);
                parameter = hp;
            }
            else if(annotation instanceof CookieValue) {
                CookieValue param = (CookieValue) annotation;
                CookieParameter cp = new CookieParameter()
                        .name(param.value());
                if(!defaultValue.isEmpty())
                    cp.setDefaultValue(defaultValue);
                Property schema = ModelConverters.getInstance().readAsProperty(cls);
                if(schema != null)
                    cp.setProperty(schema);
                parameter = cp;
            }
//            else if(annotation instanceof RequestParam) {
//                FormParam param = (FormParam) annotation;
//                FormParameter fp = new FormParameter()
//                        .name(param.value());
//                if(!defaultValue.isEmpty())
//                    fp.setDefaultValue(defaultValue);
//                Property schema = ModelConverters.getInstance().readAsProperty(cls);
//                if(schema != null)
//                    fp.setProperty(schema);
//                parameter = fp;
//            }
        }
        if(parameter != null) {
            parameters.add(parameter);
        }

        return parameters;
    }

    @Override
    public boolean shouldIgnoreClass(Class<?> cls) {
        boolean output = false;
        if(cls.getName().startsWith("org.springframework"))
            output = true;
        else
            output = false;
        return output;
    }
}
