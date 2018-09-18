package com.github.kongchen.swagger.docgen.spring;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.wordnik.sample.model.PaginationHelper;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.models.parameters.Parameter;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class SpringSwaggerExtensionTest {
    @Test
    public void testExtractParametersReturnsRetrievedParameters() {
        List<Parameter> parameters = new SpringSwaggerExtension(new SystemStreamLog()).extractParameters(
                Lists.newArrayList(getTestAnnotation()),
                PaginationHelper.class,
                Sets.<Type>newHashSet(),
                Lists.<SwaggerExtension>newArrayList().iterator());

        assertEquals(parameters.size(), 2);
    }

    @Test
    public void testExtractParametersNoModelAttributeAnnotation() {
        List<Parameter> parameters = new SpringSwaggerExtension(new SystemStreamLog()).extractParameters(
                Lists.newArrayList(),
                PaginationHelper.class,
                Sets.<Type>newHashSet(),
                Lists.<SwaggerExtension>newArrayList().iterator());

        assertEquals(parameters.size(), 2);
    }

    private Annotation getTestAnnotation() {
        return MethodUtils.getMethodsWithAnnotation(SpringSwaggerExtensionTest.SomeResource.class, ModelAttribute.class)[0].getAnnotation(ModelAttribute.class);
    }

    private static class SomeResource {
        @ModelAttribute
        public void get() {
            // no implementation needed. Method is only for the test cases, so that the annotation ModelAttribute
            // can easily be retrieved and used
        }
    }
}
