package com.wordnik.jaxrs;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.reader.JaxrsReader;
import io.swagger.models.Swagger;

import java.util.Set;

/**
 * @author Igor Gursky
 *         11.12.2015.
 */
public class CustomJaxrsReader extends JaxrsReader {
    public CustomJaxrsReader(Swagger swagger, LogAdapter LOG) {
        super(swagger, LOG);
    }

    @Override
    public Swagger read(Set<Class<?>> classes) {
        Swagger swagger = super.read(classes);
        swagger.getInfo().setDescription("Processed with CustomJaxrsReader");
        return swagger;
    }
}