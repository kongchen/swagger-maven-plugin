package com.wordnik.jaxrs;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;

import java.util.Set;

/**
 * @author Igor Gursky
 *         11.12.2015.
 */
public class CustomJaxrsReader extends VendorExtensionsJaxrsReader {

    public CustomJaxrsReader(Swagger swagger, Log LOG) {
        super(swagger, LOG, ApiSource.PREFER_SWAGGER_VALUES_DEFAULT);
    }

    @Override
    public Swagger read(Set<Class<?>> classes) {
        Swagger swagger = super.read(classes);
        swagger.getInfo().setDescription("Processed with CustomJaxrsReader");
        return swagger;
    }
}
