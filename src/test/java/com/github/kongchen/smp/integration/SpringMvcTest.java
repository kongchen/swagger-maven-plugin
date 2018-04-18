package com.github.kongchen.smp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.github.kongchen.smp.integration.utils.TestUtils.YamlToJson;
import static com.github.kongchen.smp.integration.utils.TestUtils.changeDescription;
import static com.github.kongchen.smp.integration.utils.TestUtils.createTempDirPath;
import static com.github.kongchen.smp.integration.utils.TestUtils.setCustomReader;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;

import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import junitx.framework.FileAssert;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import net.javacrumbs.jsonunit.core.Configuration;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * @author chekong on 8/15/14.
 */
public class SpringMvcTest extends AbstractMojoTestCase {
    private final File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui-spring");
    private final File docOutput = new File(getBasedir(), "generated/document-spring.html");
    private ApiDocumentMojo mojo;
    private List<SwaggerExtension> extensions;

    @Override
	@BeforeMethod
    protected void setUp() throws Exception {
    	extensions = new ArrayList<SwaggerExtension>(SwaggerExtensions.getExtensions());
    	super.setUp();

        try {
            FileUtils.deleteDirectory(swaggerOutputDir);
            FileUtils.forceDelete(docOutput);
        } catch (final Exception e) {
            //ignore
        }

        final File testPom = new File(getBasedir(), "target/test-classes/plugin-config-springmvc.xml");
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
        assertGeneratedSwaggerSpecJson("This is a sample.");
    }

    @Test
    public void testGeneratedSwaggerSpecYaml() throws Exception {
        assertGeneratedSwaggerSpecYaml("This is a sample.");
    }

    @Test
    public void testSwaggerCustomReaderJson() throws Exception {
        setCustomReader(mojo, "com.wordnik.springmvc.CustomSpringMvcReader");
        assertGeneratedSwaggerSpecJson("Processed with CustomSpringMvcReader");
    }

    @Test
    public void testSwaggerCustomReaderYaml() throws Exception {
        setCustomReader(mojo, "com.wordnik.springmvc.CustomSpringMvcReader");
        assertGeneratedSwaggerSpecYaml("Processed with CustomSpringMvcReader");
    }

    @Test
    public void testInvalidCustomReaderJson() throws Exception {
        final String className = "com.wordnik.nonexisting.Class";

        setCustomReader(mojo, className);
        try {
            testGeneratedSwaggerSpecJson();
        } catch (final MojoFailureException e) {
            assertEquals(String.format("Cannot load Swagger API reader: %s", className), e.getMessage());
        }
    }

    @Test
    public void testInvalidCustomReaderYaml() throws Exception {
        final String className = "com.wordnik.nonexisting.Class";
        setCustomReader(mojo, className);
        try {
            testGeneratedSwaggerSpecJson();
        } catch (final MojoFailureException e) {
            assertEquals(String.format("Cannot load Swagger API reader: %s", className), e.getMessage());
        }
    }

    @Test
    public void testGeneratedDoc() throws Exception {
        mojo.execute();

        final BufferedReader actualReader = null;
        final BufferedReader expectReader = null;
        FileInputStream swaggerJson = null;
        BufferedReader swaggerReader = null;

        try {
            final File actual = docOutput;
            final File expected = new File(this.getClass().getResource("/sample-springmvc.html").getFile());
            FileAssert.assertEquals(expected, actual);

            swaggerJson = new FileInputStream(new File(swaggerOutputDir, "swagger.json"));
            swaggerReader = new BufferedReader(new InputStreamReader(swaggerJson));
            String s = swaggerReader.readLine();
            while (s != null) {
                if (s.contains("\"parameters\" : [ ],")) {
                    assertFalse("should not have null parameters", true);
                }
                s = swaggerReader.readLine();
            }

        } finally {
            if (actualReader != null) {
                actualReader.close();
            }
            if (expectReader != null) {
                expectReader.close();
            }
            if (swaggerJson != null) {
                swaggerJson.close();
            }
            if (swaggerReader != null) {
                swaggerReader.close();
            }
        }

    }

    @Test
    public void testGeneratedDocWithJsonExampleValues() throws Exception {

        final List<ApiSource> apisources = (List<ApiSource>) getVariableValueFromObject(mojo, "apiSources");
        final ApiSource apiSource = apisources.get(0);
        // Force serialization of example values as json raw values
        apiSource.setJsonExampleValues(true);
        // exclude part of the model when not compliant with jev option (e.g. example expressed as plain string)
        apiSource.setApiModelPropertyExclusions(Collections.singletonList("exclude-when-jev-option-set"));

        mojo.execute();

        // check generated swagger json file
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, "swagger.json"));
        final JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/options/jsonExampleValues/expected/swagger-spring.json"));

        final JsonNode actualUserNode = actualJson.path("definitions").path("User");
        final JsonNode expectUserNode = expectJson.path("definitions").path("User");

        // Cannot test full node equality since tags order is not guaranteed in generated json
        Assert.assertEquals(actualUserNode, expectUserNode);
    }

    @Test
    public void testNullSwaggerOutput() throws Exception {
        final List<ApiSource> apisources = (List<ApiSource>) getVariableValueFromObject(mojo, "apiSources");
        apisources.get(0).setSwaggerDirectory(null);
        setVariableValueToObject(mojo, "apiSources", apisources);
        mojo.execute();
        Assert.assertFalse(swaggerOutputDir.exists());
    }

    @Test
    public void testNullMustacheOutput() throws Exception {
        final List<ApiSource> apisources = (List<ApiSource>) getVariableValueFromObject(mojo, "apiSources");
        apisources.get(0).setTemplatePath(null);
        setVariableValueToObject(mojo, "apiSources", apisources);
        mojo.execute();
        Assert.assertFalse(docOutput.exists());
    }

    @DataProvider
    private Iterator<Object[]> pathProvider() throws Exception {
        final String tempDirPath = createTempDirPath();

        final List<Object[]> dataToBeReturned = new ArrayList<Object[]>();
        dataToBeReturned.add(new String[]{tempDirPath + "foo" + File.separator + "bar" + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "bar" + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{"test.html"});

        return dataToBeReturned.iterator();
    }

    @Test(enabled = false, dataProvider = "pathProvider")
    public void testExecuteDirectoryCreated(final String path) throws Exception {
        mojo.getApiSources().get(0).setOutputPath(path);

        final File file = new File(path);
        mojo.execute();
        Assert.assertTrue(file.exists());
        if (file.getParentFile() != null) {
            FileUtils.deleteDirectory(file.getParentFile());
        }
    }

    private void assertGeneratedSwaggerSpecJson(final String description) throws MojoExecutionException, MojoFailureException, IOException {
        mojo.execute();
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, "swagger.json"));
        final JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/expectedOutput/swagger-spring.json"));

        changeDescription(expectJson, description);
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

    private void assertGeneratedSwaggerSpecYaml(final String description) throws MojoExecutionException, MojoFailureException, IOException {
        mojo.getApiSources().get(0).setOutputFormats("yaml");
        mojo.execute();

        final String actualYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(FileUtils.readFileToString(new File(swaggerOutputDir, "swagger.yaml"))));
        final String expectYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(this.getClass().getResourceAsStream("/expectedOutput/swagger-spring.yaml")));

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode actualJson = mapper.readTree(YamlToJson(actualYaml));
        final JsonNode expectJson = mapper.readTree(YamlToJson(expectYaml));

        changeDescription(expectJson, description);
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

}
