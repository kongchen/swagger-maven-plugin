package com.wordnik.sample;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.wordnik.sample.TestVendorExtension.TestVendorAnnotation;

import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.Operation;
import io.swagger.models.Response;

/**
 * Custom swagger extension which will be configured using the <code>&lt;swaggerExtension&gt;</code> tag.
 */
public class VendorExtensionWithoutReader extends AbstractSwaggerExtension {

    private static final String RESPONSE_DESCRIPTION = "Some vendor error description added using swaggerExtension";

    private static final String RESPONSE_STATUS_501 = "501";

    @Override
    public void decorateOperation(final Operation operation, final Method method, final Iterator<SwaggerExtension> chain) {

        final TestVendorAnnotation annotation = method.getAnnotation(TestVendorAnnotation.class);
        if (annotation != null) {

            Map<String, Response> map = new HashMap<String, Response>(operation.getResponses());
            final Response value = new Response();
            value.setDescription(RESPONSE_DESCRIPTION);
            map.put(RESPONSE_STATUS_501, value);
            operation.setResponses(map);
        }

        if (chain.hasNext()) {
            chain.next().decorateOperation(operation, method, chain);
        }
    }
    
}
