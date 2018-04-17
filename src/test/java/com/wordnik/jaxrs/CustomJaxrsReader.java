package com.wordnik.jaxrs;

import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;

import java.util.Set;

/**
 * @author Igor Gursky
 *         11.12.2015.
 */
public class CustomJaxrsReader extends VendorExtensionsJaxrsReader {

    public CustomJaxrsReader(final Swagger swagger, final Log LOG) {
        super(swagger, LOG);
    }

    @Override
    public Swagger read(final Set<Class<?>> classes) {
        final Swagger swagger = super.read(classes);
        swagger.getInfo().setDescription("Processed with CustomJaxrsReader");
        return swagger;
    }
}
