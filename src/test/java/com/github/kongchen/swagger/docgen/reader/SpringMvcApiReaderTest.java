package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Example;
import io.swagger.annotations.ExampleProperty;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertEquals;

public class SpringMvcApiReaderTest {
    @Mock
    private Log log;

    private SpringMvcApiReader reader;

    @BeforeMethod
    public void setup() {
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
    public void exampleProperty() throws GenerateException {
        Swagger result = reader.read(Collections.singleton(SwaggerApi.class));

        assertNotNull(result);
        assertFalse(result.getPaths().isEmpty());
        Path path = result.getPaths().values().iterator().next();
        assertNotNull(path);
        assertNotNull(path.getGet());
        assertFalse(path.getGet().getResponses().isEmpty());
        Response getResponse = path.getGet().getResponses().values().iterator().next();
        assertNotNull(getResponse);
        assertFalse(getResponse.getExamples().isEmpty());
        Object exampleValue = getResponse.getExamples().get(APPLICATION_JSON_VALUE);
        Assert.assertEquals(exampleValue, "Some example of a string");
    }

    @Api(tags = "tag1", description = "This is tag1 description.")
    @RequestMapping("/swagger-api")
    static class SwaggerApi {
        @ApiResponses({@ApiResponse(code = 200, message = "Successful request - see response for 'pong'",
                examples = @Example(@ExampleProperty(mediaType = APPLICATION_JSON_VALUE, value = "Some example of a string")))})
        @GetMapping
        public String testMethod() {
            return "test";
        }
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
}
