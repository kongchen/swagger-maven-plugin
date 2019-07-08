package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.spring.SpringResource;

import io.swagger.models.HttpMethod;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mockito;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;

public class SpringMvcApiReaderTest {

    @Test
    public void testMethodsInheritingPathFromClassLevelRequestMapping() throws GenerateException {
        Swagger swagger = new Swagger();
        SpringMvcApiReader reader = new SpringMvcApiReader(swagger, null);
        Set<Class<?>> classes = Collections.singleton( SomeResourceWithClassOnlyPaths.class );
        Map<String, SpringResource> resourceMap = reader.generateResourceMap(classes);
        assertEquals(3, resourceMap.size());
    }

    @Test
    public void testDeprecatedAnnotationOnControllerClass() throws GenerateException {
        Swagger swagger = new Swagger();
        SpringMvcApiReader reader = new SpringMvcApiReader(swagger, Mockito.mock(Log.class));
        reader.read(Collections.singleton(ADeprecatedResource.class));
        assertTrue(swagger.getPath("/adeprecatedpath").getOperationMap().get(HttpMethod.GET).isDeprecated(), "This operation should be marked as deprecated");
    }

    @RequestMapping("/some/path")
    private static class SomeResourceWithClassOnlyPaths {

        // GET /some/path (explicit value="")
        @RequestMapping(value="", method=RequestMethod.GET)
        public String get() { return null; }

        // POST /some/path (value=null)
        @RequestMapping(method=RequestMethod.POST)
        public void post() { }

        // GET /some/path/search
        @RequestMapping(value="/search", method=RequestMethod.GET)
        public String search() { return null; }
    }

    @Deprecated
    @RequestMapping("/adeprecatedpath")
    private static class ADeprecatedResource {

        // GET /some/path (explicit value="")
        @RequestMapping(method = RequestMethod.GET)
        public String get() { return null; }

    }
}
