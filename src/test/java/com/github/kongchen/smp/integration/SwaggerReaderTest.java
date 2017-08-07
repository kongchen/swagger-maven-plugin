package com.github.kongchen.smp.integration;

import static com.github.kongchen.smp.integration.utils.TestUtils.YamlToJson;
import static com.github.kongchen.smp.integration.utils.TestUtils.changeDescription;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;

import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import net.javacrumbs.jsonunit.core.Configuration;

/**
 * @author chekong on 8/15/14.
 */
public class SwaggerReaderTest extends AbstractMojoTestCase {
    private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui");
    private ApiDocumentMojo mojo;
    private ObjectMapper mapper = new ObjectMapper();
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

        File testPom = new File(getBasedir(), "target/test-classes/plugin-config-swaggerreader.xml");
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
        executeAndAssertGeneratedSwaggerSpecJson("This is a sample.", "/expectedOutput/swagger-swaggerreader.json");
    }

    @Test
    public void testGeneratedSwaggerSpecYaml() throws Exception {
        assertGeneratedSwaggerSpecYaml("This is a sample.", "/expectedOutput/swagger-swaggerreader.yaml");
    }

    private void executeAndAssertGeneratedSwaggerSpecJson(String description, String expectedOutput) throws MojoExecutionException, MojoFailureException, IOException {
        mojo.execute();
        assertGeneratedSwaggerSpecJson(description, expectedOutput, "swagger.json");
    }

    private void assertGeneratedSwaggerSpecJson(String description, String expectedOutput, String generatedFileName) throws IOException {
        JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, generatedFileName));
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream(expectedOutput));

        changeDescription(expectJson, description);
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

    private void assertGeneratedSwaggerSpecYaml(String description, String expectedOutput) throws MojoExecutionException, MojoFailureException, IOException {
        mojo.getApiSources().get(0).setOutputFormats("yaml");
        mojo.execute();

        String actualYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(FileUtils.readFileToString(new File(swaggerOutputDir, "swagger.yaml"))));
        String expectYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(this.getClass().getResourceAsStream(expectedOutput)));

        JsonNode actualJson = mapper.readTree(YamlToJson(actualYaml));
        JsonNode expectJson = mapper.readTree(YamlToJson(expectYaml));

        changeDescription(expectJson, description);
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }
}
