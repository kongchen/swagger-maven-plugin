package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.testng.Assert.*;

/**
 * @author Dusan Markovic
 */
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
        assertEquals(exampleValue, "Some example of a string");
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
}
