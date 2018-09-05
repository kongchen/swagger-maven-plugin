package com.github.kongchen.swagger.docgen.reader;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.maven.plugin.logging.Log;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.ArrayModel;
import io.swagger.models.Operation;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;

public class JaxrsReaderTest {
    @Mock
    private Log log;

    private JaxrsReader reader;

    List<SwaggerExtension> extensions = SwaggerExtensions.getExtensions();

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        reader = new JaxrsReader(new Swagger(), log);
    }

    @AfterMethod
    public void resetExtenstions() {
        SwaggerExtensions.setExtensions(extensions);
    }

    @Test
    public void ignoreClassIfNoApiAnnotation() {
        Swagger result = reader.read(NotAnnotatedApi.class);

        assertEmptySwaggerResponse(result);
    }

    @Test
    public void ignoreApiIfHiddenAttributeIsTrue() {
        Swagger result = reader.read(HiddenApi.class);

        assertEmptySwaggerResponse(result);
    }

    @Test
    public void includeApiIfHiddenParameterIsTrueAndApiHiddenAttributeIsTrue() {
        Swagger result = reader.read(HiddenApi.class, "", null, true, new String[0], new String[0], new HashMap<String, Tag>(), new ArrayList<Parameter>());

        assertNotNull(result, "No Swagger object created");
        assertFalse(result.getTags().isEmpty(), "Should contain api tags");
        assertFalse(result.getPaths().isEmpty(), "Should contain operation paths");
    }

    @Test
    public void discoverApiOperation() {
        Tag expectedTag = new Tag();
        expectedTag.name("atag");
        Swagger result = reader.read(AnApi.class);

        assertSwaggerResponseContents(expectedTag, result);
    }

    @Test
    public void createNewSwaggerInstanceIfNoneProvided() {
        JaxrsReader nullReader = new JaxrsReader(null, log);
        Tag expectedTag = new Tag();
        expectedTag.name("atag");
        Swagger result = nullReader.read(AnApi.class);

        assertSwaggerResponseContents(expectedTag, result);
    }

    @Test
    public void handleOctetStreamAndByteArray() {
        Swagger result = reader.read(AnApiWithOctetStream.class);
        io.swagger.models.Path path = result.getPaths().get("/apath/add");
        assertNotNull(path, "Expecting to find a path ..");
        assertNotNull(path.getPost(), ".. with post opertion ..");
        assertNotNull(path.getPost().getConsumes().contains("application/octet-stream"), ".. and with octect-stream consumer.");
        assertTrue(path.getPost().getParameters().get(0) instanceof BodyParameter, "The parameter is a body parameter ..");
        assertFalse(((BodyParameter) path.getPost().getParameters().get(0)).getSchema() instanceof ArrayModel, " .. and the schema is NOT an ArrayModel");
    }

    private void assertEmptySwaggerResponse(Swagger result) {
        assertNotNull(result, "No Swagger object created");
        assertNull(result.getTags(), "Should not have any tags");
        assertNull(result.getPaths(), "Should not have any paths");
    }

    private void assertSwaggerResponseContents(Tag expectedTag, Swagger result) {
        assertNotNull(result, "No Swagger object created");
        assertFalse(result.getTags().isEmpty(), "Should contain api tags");
        assertTrue(result.getTags().contains(expectedTag), "Expected tag missing");
        assertFalse(result.getPaths().isEmpty(), "Should contain operation paths");
        assertTrue(result.getPaths().containsKey("/apath"), "Path missing from paths map");
        io.swagger.models.Path path = result.getPaths().get("/apath");
        assertFalse(path.getOperations().isEmpty(), "Should be a get operation");
    }

    @Test
    public void discoverSubResource() {
        Swagger result = reader.read(SomeResource.class);
        assertSwaggerPath(result.getPath("/resource/explicit/name").getGet(), result, "/resource/implicit/name");
    }

    private void assertSwaggerPath(Operation expectedOperation, Swagger result, String expectedPath) {
        assertNotNull(result, "No Swagger object created");
        assertFalse(result.getPaths().isEmpty(), "Should contain operation paths");
        assertTrue(result.getPaths().containsKey(expectedPath), "Expected path missing");
        io.swagger.models.Path path = result.getPaths().get(expectedPath);
        assertFalse(path.getOperations().isEmpty(), "Should be a get operation");
        assertEquals(expectedOperation, path.getGet(), "Should contain operation");
    }

    @Api(tags = "atag")
    @Path("/apath")
    static class AnApi {
        @ApiOperation(value = "Get a model.")
        @GET
        public Response getOperation() {
            return Response.ok().build();
        }
    }

    @Api(hidden = true, tags = "atag")
    @Path("/hidden/path")
    static class HiddenApi {
        @ApiOperation(value = "Get a model.")
        @GET
        public Response getOperation() {
            return Response.ok().build();
        }
    }

    @Path("/apath")
    static class NotAnnotatedApi {
    }


    @Api(value = "v1")
    @Path("/apath")
    static class AnApiWithOctetStream {
        @POST
        @Path("/add")
        @ApiOperation(value = "Add content")
        @Consumes(MediaType.APPLICATION_OCTET_STREAM)
        public void addOperation(
                @ApiParam(value = "content", required = true, type = "string", format = "byte")
                    final byte[] content) {
        }
    }

    @Path("/resource")
    @Api(tags = "Resource")
    static class SomeResource {
        @Path("explicit")
        public SomeSubResource getSomething() {
            // no implementation needed. Method is only for the test cases, so that the return type is captured
            return new SomeSubResource();
        }
        @Path("implicit")
        @ApiOperation(value="", response = SomeSubResource.class)
        public Object getSomeSub() {
            // no implementation needed. Method is only for the test cases, so that the return type is overridden by @ApiOperation.response
            return new SomeSubResource();
        }
    }

    static class SomeSubResource {
        @Path("name")
        @GET
        public String getName() {
            // no implementation needed. Method is only for the test cases
            return toString();
        }
    }

}
