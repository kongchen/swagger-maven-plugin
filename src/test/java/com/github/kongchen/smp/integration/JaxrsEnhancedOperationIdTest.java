package com.github.kongchen.smp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.util.Json;
import net.javacrumbs.jsonunit.core.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

public class JaxrsEnhancedOperationIdTest extends AbstractMojoTestCase {
    private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui-enhanced-operation-id");
    private ApiDocumentMojo mojo;
    private ObjectMapper mapper = Json.mapper();
    private List<SwaggerExtension> extensions;

    @Override
	@BeforeMethod
    protected void setUp() throws Exception {
    	extensions = new ArrayList<SwaggerExtension>(SwaggerExtensions.getExtensions());
    	super.setUp();

        try {
            FileUtils.deleteDirectory(swaggerOutputDir);
        } catch(Exception e) {
            //ignore
        }

        File testPom = new File(getBasedir(), "target/test-classes/plugin-config-enhanced-operation-id.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
    }
    
    @Override
    @AfterMethod
    protected void tearDown() throws Exception {
    	super.tearDown();
    	SwaggerExtensions.setExtensions(extensions);
    }

    @Test
    public void testGeneratedSwaggerSpecJson() throws Exception {
        mojo.execute();
        JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, "swagger.json"));
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/expectedOutput/swagger-enhanced-operation-id.json"));

        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }
}
