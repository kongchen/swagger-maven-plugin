package com.github.kongchen.smp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import net.javacrumbs.jsonunit.core.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;

import static com.github.kongchen.smp.integration.utils.TestUtils.YamlToJson;
import static com.github.kongchen.smp.integration.utils.TestUtils.changeDescription;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

/**
 * @author chekong on 8/15/14.
 */
public class SwaggerReaderTest extends AbstractMojoTestCase {
    private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui");
    private ApiDocumentMojo mojo;
    private ObjectMapper mapper = new ObjectMapper();

    @Override
	@BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();

        try {
            FileUtils.deleteDirectory(swaggerOutputDir);
        } catch(Exception e) {
            //ignore
        }

        File testPom = new File(getBasedir(), "target/test-classes/plugin-config-swaggerreader.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
    }

    @Test
    public void testGeneratedSwaggerSpecJson() throws Exception {
        mojo.execute();

        JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, "swagger.json"));
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/expectedOutput/swagger-swaggerreader.json"));

        changeDescription(expectJson, "This is a sample.");
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testGeneratedSwaggerSpecYaml() throws Exception {
        mojo.getApiSources().get(0).setOutputFormats("yaml");
        mojo.execute();

        String actualYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(FileUtils.readFileToString(new File(swaggerOutputDir, "swagger.yaml"))));
        String expectYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(this.getClass().getResourceAsStream("/expectedOutput/swagger-swaggerreader.yaml")));

        JsonNode actualJson = mapper.readTree(YamlToJson(actualYaml));
        JsonNode expectJson = mapper.readTree(YamlToJson(expectYaml));

        changeDescription(expectJson, "This is a sample.");
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

}
