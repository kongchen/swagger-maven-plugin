package com.wordnik.springmvc;

import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import com.wordnik.sample.TestVendorExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;

import java.util.LinkedList;
import java.util.List;

public class VendorExtensionsSpringMvcReader extends SpringMvcApiReader {

    public VendorExtensionsSpringMvcReader(Swagger swagger, Log log) {
        super(swagger, log);

        List<SwaggerExtension> extensions = new LinkedList<SwaggerExtension>(SwaggerExtensions.getExtensions());
        extensions.add(new TestVendorExtension());
        SwaggerExtensions.setExtensions(extensions);
    }
}
