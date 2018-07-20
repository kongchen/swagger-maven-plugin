package com.github.kongchen.swagger.docgen.reader;

import java.util.Set;

import org.apache.maven.plugin.logging.Log;

import com.github.kongchen.swagger.docgen.GenerateException;

import io.swagger.models.Swagger;
import io.swagger.servlet.Reader;

/**
 * A dedicated {@link ClassSwaggerReader} to scan Serlet classes.
 */
public class ServletReader extends AbstractReader implements ClassSwaggerReader {

    public ServletReader(Swagger swagger, Log LOG) {
        super(swagger, LOG);
    }

    @Override
    public Swagger read(Set<Class<?>> classes) throws GenerateException {
        Reader.read(swagger, classes );
        return swagger;
    }

}
