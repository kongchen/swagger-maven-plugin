package com.github.kongchen.swagger.docgen.jaxrs;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.testng.annotations.Test;

import javax.ws.rs.DefaultValue;
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
                Lists.newArrayList(getTestAnnotation("get", QueryParam.class)),
                String.class,
                Sets.<Type>newHashSet(),
                Lists.<SwaggerExtension>newArrayList().iterator());

        assertFalse(parameters.isEmpty());
        assertEquals(parameters.size(), 1);
    }


    private Annotation getTestAnnotation(String name, Class<? extends Annotation> wantedAnnotation) {
        return MethodUtils.getMatchingMethod(SomeResource.class, name, String.class).getAnnotation(wantedAnnotation);
    }

    @Test
    public void testParameterDefaultValue() {
        List<Parameter> parameters = new JaxrsParameterExtension().extractParameters(
                Lists.newArrayList(
                        getTestAnnotation("getWithDefault", QueryParam.class),
                        getTestAnnotation("getWithDefault", DefaultValue.class)
                ),
                String.class,
                Sets.<Type>newHashSet(),
                Lists.<SwaggerExtension>newArrayList().iterator());

        assertFalse(parameters.isEmpty());
        assertEquals(parameters.size(), 1);

        Parameter extracted = parameters.get(0);
        assertEquals(((AbstractSerializableParameter)extracted).getDefaultValue(), "en-US");
    }

    private static class SomeResource {
        @QueryParam("lang")
        public void get(String lang) {
            // no implementation needed. Method is only for the test cases, so that the annotation QueryParam
            // can easily be retrieved and used
        }

        @QueryParam("lang")
        @DefaultValue("en-US")
        public void getWithDefault(String lang) {
            // Needed for testing default values with jaxrs
        }
    }
}
