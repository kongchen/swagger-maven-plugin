package com.github.kongchen.swagger.docgen.jaxrs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.testng.annotations.Test;

import javax.ws.rs.QueryParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class JaxrsParameterExtensionTest {
    @Test
    public void testExtractParametersReturnsRetrievedParameters() {
        List<Parameter> parameters = new JaxrsParameterExtension().extractParameters(
                Lists.newArrayList(getTestAnnotation()),
                String.class,
                Sets.<Type>newHashSet(),
                Lists.<SwaggerExtension>newArrayList().iterator());

        assertFalse(parameters.isEmpty());
        assertEquals(parameters.size(), 1);
    }

    private Annotation getTestAnnotation() {
        return MethodUtils.getMethodsWithAnnotation(SomeResource.class, QueryParam.class)[0].getAnnotation(QueryParam.class);
    }

    private static class SomeResource {
        @QueryParam("lang")
        public void get(String lang) {
            // no implementation needed. Method is only for the test cases, so that the annotation QueryParam
            // can easily be retrieved and used
        }
    }
}
