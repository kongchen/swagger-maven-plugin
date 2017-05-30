package com.wordnik.sample;

import io.swagger.jaxrs.ext.AbstractSwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.Operation;
import io.swagger.models.Response;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @see com.wordnik.jaxrs.VendorExtensionsJaxrsReader
 * @see com.wordnik.springmvc.VendorExtensionsSpringMvcReader
 */
public class TestVendorExtension extends AbstractSwaggerExtension {

    private static final String RESPONSE_DESCRIPTION = "Some vendor error description";

    private static final String RESPONSE_STATUS_401 = "401";

    @Override
    public void decorateOperation(final Operation operation, final Method method, final Iterator<SwaggerExtension> chain) {

        final TestVendorAnnotation annotation = method.getAnnotation(TestVendorAnnotation.class);
        if (annotation != null) {

            Map<String, Response> map = new HashMap<String, Response>(operation.getResponses());
            final Response value = new Response();
            value.setDescription(RESPONSE_DESCRIPTION);
            map.put(RESPONSE_STATUS_401, value);
            operation.setResponses(map);
        }

        if (chain.hasNext()) {
            chain.next().decorateOperation(operation, method, chain);
        }
    }

    /**
     * Processed by {@link TestVendorExtension}
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface TestVendorAnnotation {}
}
