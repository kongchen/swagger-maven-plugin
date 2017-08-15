package com.github.kongchen.swagger.docgen.reader;

import java.util.Set;

import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.GenerateException;

import io.swagger.jaxrs.Reader;
import io.swagger.models.Swagger;

/**
 * This API reader is directly using the swagger internal {@link Reader} to scan the classes.
 * This reader is used when the exact output as the runtime generated swagger file is necessary.
 */
public class SwaggerReader extends AbstractReader implements ClassSwaggerReader {

    public SwaggerReader(Swagger swagger, Log LOG) {
        super(swagger, LOG);
    }

    @Override
    public Swagger read(Set<Class<?>> classes) throws GenerateException {
        return new Reader(swagger).read(classes);
    }

}
