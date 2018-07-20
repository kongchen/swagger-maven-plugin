package com.github.kongchen.swagger.docgen.reader;

import org.apache.maven.plugin.logging.Log;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.HttpStatus.LOCKED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

public class SpringExceptionHandlerReaderTest {
    @Mock
    protected Log log;

    private SpringExceptionHandlerReader exceptionHandlerReader;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        exceptionHandlerReader = new SpringExceptionHandlerReader(log);
    }

    @Test
    public void getResponseStatusesFromExceptions() throws NoSuchMethodException {
        List<ResponseStatus> result = exceptionHandlerReader.getResponseStatusesFromExceptions(
                this.getClass().getMethod("tryGetIt"));
        assertThat(result, hasItem(withValue(equalTo(NOT_FOUND))));
    }

    @Test
    public void getResponseStatusesFromExceptionsReturnsEmptyList() throws NoSuchMethodException {
        List<ResponseStatus> result = exceptionHandlerReader.getResponseStatusesFromExceptions(
                this.getClass().getMethod("safeToPost"));
        assertThat(result, hasSize(0));
    }

    @Test
    public void getResponseStatusFromExceptionHandler() throws NoSuchMethodException {
        Set<Class<?>> classesToRead = new HashSet<Class<?>>();
        classesToRead.add(CustomExceptionHandler.class);
        exceptionHandlerReader.processExceptionHandlers(classesToRead);
        List<ResponseStatus> result = exceptionHandlerReader.getResponseStatusesFromExceptions(
                this.getClass().getMethod("overridenInHandler"));
        assertThat(result, hasItem(withValue(equalTo(LOCKED))));
    }

    @RequestMapping(value = "/", method = GET)
    public ResponseEntity tryGetIt() throws NotFoundException {
        return new ResponseEntity(OK);
    }

    @RequestMapping(value = "/", method = POST)
    public ResponseEntity safeToPost() {
        return new ResponseEntity(OK);
    }

    @RequestMapping(value = "/", method = PUT)
    public ResponseEntity overridenInHandler() throws OverridenInHandlerException {
        return null;
    }

    @ControllerAdvice
    class CustomExceptionHandler {
        @ExceptionHandler(OverridenInHandlerException.class)
        @ResponseStatus(LOCKED)
        public void handle(OverridenInHandlerException exception) {
        }
    }

    @ResponseStatus(NOT_FOUND)
    class NotFoundException extends Exception {
    }

    @ResponseStatus(OK)
    class OverridenInHandlerException extends Exception {
    }

    private static Matcher<ResponseStatus> withValue(Matcher<HttpStatus> submatcher) {
        return new FeatureMatcher<ResponseStatus, HttpStatus>(submatcher, "", "") {
            @Override
            protected HttpStatus featureValueOf(ResponseStatus responseStatus) {
                return responseStatus.value();
            }
        };
    }
}
