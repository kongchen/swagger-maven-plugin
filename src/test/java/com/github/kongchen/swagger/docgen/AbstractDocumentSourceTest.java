package com.github.kongchen.swagger.docgen;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class AbstractDocumentSourceTest {
    @Mock
    private Log log;
    @Mock
    private ApiSource apiSource;
    private AbstractDocumentSource source;

    @BeforeMethod
    public void setUp() throws MojoFailureException {
        MockitoAnnotations.initMocks(this);
        source = new AbstractDocumentSource(log, apiSource) {
            @Override
            protected ClassSwaggerReader resolveApiReader() throws GenerateException {
                return null;
            }
        };
    }

    @Test
    public void removeBasePathFromEndpoints() {
        // arrange
        Swagger swagger = new Swagger();
        Map<String, Path> pathMap = new HashMap<String, Path>();
        pathMap.put("/a/b/c", new Path());
        swagger.setPaths(pathMap);
        swagger.setBasePath("/a/b");

        // act
        Swagger result = source.removeBasePathFromEndpoints(swagger, true);

        // assert
        assertThat(result.getPath("/c"), notNullValue());
        assertThat(result.getPath("/a/b/c"), nullValue());
    }
}
