package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import io.swagger.annotations.Api;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertEquals;

public class SpringMvcApiReaderTest {
    @Mock
    private Log log;

    private SpringMvcApiReader reader;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        reader = new SpringMvcApiReader(new Swagger(), log);
    }

    @Test
    public void testMethodsInheritingPathFromClassLevelRequestMapping() throws GenerateException {
        Swagger swagger = new Swagger();
        SpringMvcApiReader reader = new SpringMvcApiReader(swagger, null);
        Set<Class<?>> classes = Collections.singleton(SomeResourceWithClassOnlyPaths.class);
        Map<String, SpringResource> resourceMap = reader.generateResourceMap(classes);
        assertEquals(3, resourceMap.size());
    }

    @Test
    public void singleTagDescription() throws GenerateException {
        Swagger result = reader.read(Collections.singleton(SwaggerApi.class));

        assertNotNull(result);
        assertFalse(result.getTags().isEmpty());
        Tag tag = result.getTags().get(0);
        assertNotNull(tag);
        assertNotNull(tag.getDescription());
        Assert.assertEquals(tag.getDescription(), "This is tag1 description.");
    }

    @Test
    public void multipleTagsDescriptionNull() throws GenerateException {
        Swagger result = reader.read(Collections.singleton(SwaggerApiMutipleTags.class));

        assertNotNull(result);
        assertFalse(result.getTags().isEmpty());
        Tag tag = result.getTags().get(0);
        assertNotNull(tag);
        assertNull(tag.getDescription());
    }

    @RequestMapping("/some/path")
    private static class SomeResourceWithClassOnlyPaths {

        // GET /some/path (explicit value="")
        @RequestMapping(value = "", method = RequestMethod.GET)
        public String get() {
            return null;
        }

        // POST /some/path (value=null)
        @RequestMapping(method = RequestMethod.POST)
        public void post() {
        }

        // GET /some/path/search
        @RequestMapping(value = "/search", method = RequestMethod.GET)
        public String search() {
            return null;
        }
    }


    @Api(tags = "tag1", description = "This is tag1 description.")
    @RequestMapping("/swagger-api")
    static class SwaggerApi {
        @PutMapping
        public void testVoidMethod() {
        }
    }

    @Api(tags = {"tag2", "tag3"}, description = "This description should not work.")
    @RequestMapping("/swagger-api1")
    static class SwaggerApiMutipleTags {
        @PutMapping
        public void testVoidMethod1() {
        }
    }
}
