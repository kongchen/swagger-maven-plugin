package com.github.kongchen.swagger.docgen.reader;

import io.swagger.annotations.ApiOperation;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Type;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbstractReaderTest {
    private static final String ANNOTATED_METHOD = "Annotated method";

    @Mock
    private Log log;
    @Mock
    private Swagger swagger;

    private AbstractReader reader;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        reader = new AbstractReader(swagger, log) {
            @Override
            public Set<Type> getTypesToSkip() {
                return super.getTypesToSkip();
            }
        };
    }

    @ApiOperation(ANNOTATED_METHOD)
    public final void annotatedMethod() {
    }

    public final void notAnnotatedMethod() {
    }

    @Test
    public void getApiOperationReturnsMethodAnnotation() throws NoSuchMethodException {
        ApiOperation result = reader.getApiOperation(getClass().getMethod("annotatedMethod"));
        assertThat(result.value(), is(ANNOTATED_METHOD));
    }

    @Test
    public void getApiOperationReturnsDefaultAnnotation() throws NoSuchMethodException {
        ApiOperation result = reader.getApiOperation(getClass().getMethod("notAnnotatedMethod"));
        assertThat(result.value(), is(""));
    }

}
