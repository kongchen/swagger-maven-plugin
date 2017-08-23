package com.wordnik.jaxrs;

import com.github.kongchen.swagger.docgen.reader.JaxrsReader;
import com.wordnik.sample.TestVendorExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;

import java.util.LinkedList;
import java.util.List;

public class VendorExtensionsJaxrsReader extends JaxrsReader {

    public VendorExtensionsJaxrsReader(Swagger swagger, Log log) {
        this(swagger, log, true);
    }

    public VendorExtensionsJaxrsReader(Swagger swagger, Log log, boolean preferSwaggerValues) {
        super(swagger, log, preferSwaggerValues);

        List<SwaggerExtension> extensions = new LinkedList<SwaggerExtension>(SwaggerExtensions.getExtensions());
        extensions.add(new TestVendorExtension());
        SwaggerExtensions.setExtensions(extensions);
    }
}
