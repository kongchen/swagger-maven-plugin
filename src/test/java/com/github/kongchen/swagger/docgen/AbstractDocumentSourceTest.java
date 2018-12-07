package com.github.kongchen.swagger.docgen;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

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

    @Test
    public void testExternalDocsGetAdded() throws MojoFailureException {
        // arrange
        when(apiSource.getExternalDocs()).thenReturn(new ExternalDocs("Example external docs", "https://example.com/docs"));

        // act
        AbstractDocumentSource externalDocsSource = new AbstractDocumentSource(log, apiSource) {
            @Override
            protected ClassSwaggerReader resolveApiReader() throws GenerateException {
                return null;
            }
        };

        // assert
        assertThat(externalDocsSource.swagger.getExternalDocs(), notNullValue());
        assertThat(externalDocsSource.swagger.getExternalDocs().getDescription(), equalTo("Example external docs"));
        assertThat(externalDocsSource.swagger.getExternalDocs().getUrl(), equalTo("https://example.com/docs"));
    }

    @Test
    public void testAddDescriptionFile() throws URISyntaxException, MojoFailureException {

        // arrange
        URI fileUri = this.getClass().getResource("descriptionFile.txt").toURI();
        File descriptionFile = new File(fileUri);

        when(apiSource.getDescriptionFile()).thenReturn(descriptionFile);
        when(apiSource.getInfo()).thenReturn(new Info());

        // act
        AbstractDocumentSource externalDocsSource = new AbstractDocumentSource(log, apiSource) {
            @Override
            protected ClassSwaggerReader resolveApiReader() throws GenerateException {
                return null;
            }
        };

        // assert
        assertThat(externalDocsSource.swagger.getInfo().getDescription(), is("Description file content\n"));
    }
}
