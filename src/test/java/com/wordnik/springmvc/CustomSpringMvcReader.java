package com.wordnik.springmvc;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import io.swagger.models.Swagger;

import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.logging.Log;

/**
 * @author Igor Gursky
 *         11.12.2015.
 */
public class CustomSpringMvcReader extends VendorExtensionsSpringMvcReader {
    public CustomSpringMvcReader(final Swagger swagger, final Log log) {
        super(swagger, log);
    }

    @Override
    public Swagger read(final Set<Class<?>> classes) throws GenerateException {
        final Map<String, SpringResource> resourceMap = generateResourceMap(classes);
        for (final String str : resourceMap.keySet()) {
            final SpringResource resource = resourceMap.get(str);
            read(resource);
        }
        swagger.getInfo().setDescription("Processed with CustomSpringMvcReader");
        return swagger;
    }
}
