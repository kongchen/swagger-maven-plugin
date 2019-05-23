package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import io.swagger.models.Swagger;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.annotations.Test;
import org.codehaus.plexus.logging.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
}
