package com.github.kongchen.swagger.docgen.util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.testng.annotations.Test;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class AnnotationsTests {

    @Test
    public void shouldReturnClassAnnotationFromParent() {
        // when
        Api api = Annotations.get(TestApiB.class, Api.class);
        // then
        assertNotNull(api);
    }

    @Test
    public void shouldReturnMethodAnnotationFromParent() throws NoSuchMethodException {
        // given
        TestApiB b = new TestApiB();
        Method method = b.getClass().getMethod("getCount", null);
        // when
        ApiOperation apiOperation = Annotations.get(method, ApiOperation.class);
        // then
        assertEquals(apiOperation.value(), "Get count");

    }

    @Test
    public void shouldReturnAnnotationForOverriddenMethod() throws NoSuchMethodException {
        // given
        TestApiB b = new TestApiB();
        Method method = b.getClass().getMethod("getName", null);
        // when
        ApiOperation apiOperation = Annotations.get(method, ApiOperation.class);
        // then
        assertEquals(apiOperation.value(), "Get name");

    }

    @Test
    public void shouldNotReturnAnnotations() throws NoSuchMethodException {
        // given
        TestApiB b = new TestApiB();
        Method method = b.getClass().getMethod("getCount", null);
        // when
        Path path = Annotations.get(method, Path.class);
        // then
        assertEquals(path, null);

    }

}
