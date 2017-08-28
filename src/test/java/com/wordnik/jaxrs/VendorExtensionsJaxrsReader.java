package com.wordnik.jaxrs;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.reader.JaxrsReader;
import com.wordnik.sample.TestVendorExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;

import java.util.LinkedList;
import java.util.List;

public class VendorExtensionsJaxrsReader extends JaxrsReader {

    @Deprecated
    public VendorExtensionsJaxrsReader(Swagger swagger, Log LOG) {
        this(swagger, LOG, ApiSource.PREFER_SWAGGER_VALUES_DEFAULT);
    }

    public VendorExtensionsJaxrsReader(Swagger swagger, Log LOG, boolean preferSwaggerValues) {
        super(swagger, LOG, preferSwaggerValues);

        List<SwaggerExtension> extensions = new LinkedList<SwaggerExtension>(SwaggerExtensions.getExtensions());
        extensions.add(new TestVendorExtension());
        SwaggerExtensions.setExtensions(extensions);
    }
}
