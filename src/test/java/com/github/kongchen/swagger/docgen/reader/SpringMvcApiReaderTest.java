package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import io.swagger.annotations.Api;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;

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
    public void singleTagDescription() throws GenerateException {
        Swagger result = reader.read(Collections.singleton(SwaggerApi.class));

        assertNotNull(result);
        assertFalse(result.getTags().isEmpty());
        Tag tag = result.getTags().get(0);
        assertNotNull(tag);
        assertNotNull(tag.getDescription());
        assertEquals(tag.getDescription(), "This is tag1 description.");
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
