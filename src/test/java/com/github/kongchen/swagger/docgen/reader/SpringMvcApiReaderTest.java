package com.github.kongchen.swagger.docgen.reader;

import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

public class SpringMvcApiReaderTest {
    private static final String CONTROLLER_PATH = "/rest/service/controller/";

    @Mock
    protected Log log;
    @Mock
    protected Swagger swagger;

    private SpringMvcApiReader reader;
    private Set<Class<?>> classesToRead;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        reader = new SpringMvcApiReader(swagger, log);
        classesToRead = new HashSet<Class<?>>();
        classesToRead.add(RestServiceController.class);
    }

    @Test
    public void getResponseStatusesFromExceptions() throws NoSuchMethodException {
        List<ResponseStatus> result = reader.getResponseStatusesFromExceptions(
                RestServiceController.class.getMethod("tryGetIt"));
        assertThat(result, hasSize(1));
    }

    @Test
    public void getResponseStatusesFromExceptionsReturnsEmptyList() throws NoSuchMethodException {
        List<ResponseStatus> result = reader.getResponseStatusesFromExceptions(
                RestServiceController.class.getMethod("safeToPost"));
        assertThat(result, hasSize(0));
    }

    @ResponseStatus(NOT_FOUND)
    class NotFoundException extends Exception {
    }

    @RestController
    @RequestMapping(path = CONTROLLER_PATH, produces=APPLICATION_JSON)
    class RestServiceController {

        @RequestMapping(value = "/", method = GET)
        public ResponseEntity tryGetIt() throws SpringMvcApiReaderTest.NotFoundException {
            return new ResponseEntity(OK);
        }

        @RequestMapping(value = "/", method = POST)
        public ResponseEntity safeToPost() {
            return new ResponseEntity(OK);
        }

    }

}
