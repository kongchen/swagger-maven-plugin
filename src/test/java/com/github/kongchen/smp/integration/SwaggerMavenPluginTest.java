package com.github.kongchen.smp.integration;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.kongchen.smp.integration.utils.PetIdToStringModelConverter;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.google.common.collect.ImmutableList;

import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import net.javacrumbs.jsonunit.core.Configuration;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.github.kongchen.smp.integration.utils.TestUtils.YamlToJson;
import static com.github.kongchen.smp.integration.utils.TestUtils.changeDescription;
import static com.github.kongchen.smp.integration.utils.TestUtils.createTempDirPath;
import static com.github.kongchen.smp.integration.utils.TestUtils.setCustomReader;
import io.swagger.util.Json;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

/**
 * @author chekong on 8/15/14.
 */
public class SwaggerMavenPluginTest extends AbstractMojoTestCase {

    private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui");
    private File docOutput = new File(getBasedir(), "generated/document.html");
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
            FileUtils.forceDelete(docOutput);
        } catch(Exception e) {
            //ignore
        }

        File testPom = new File(getBasedir(), "target/test-classes/plugin-config.xml");
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
        executeAndAssertGeneratedSwaggerSpecJson("This is a sample.", "/expectedOutput/swagger.json");
    }

    @Test
    public void testGeneratedSwaggerSpecYaml() throws Exception {
        assertGeneratedSwaggerSpecYaml("This is a sample.", "/expectedOutput/swagger.yaml");
    }

    @Test
    public void testSwaggerCustomReaderJson() throws Exception {
        setCustomReader(mojo, "com.wordnik.jaxrs.CustomJaxrsReader");
        executeAndAssertGeneratedSwaggerSpecJson("Processed with CustomJaxrsReader", "/expectedOutput/swagger.json");
    }

    @Test
    public void testSwaggerCustomReaderYaml() throws Exception {
        setCustomReader(mojo, "com.wordnik.jaxrs.CustomJaxrsReader");
        assertGeneratedSwaggerSpecYaml("Processed with CustomJaxrsReader", "/expectedOutput/swagger.yaml");
    }

    @Test
    public void testInvalidCustomReaderJson() throws Exception {
        String className = "com.wordnik.nonexisting.Class";

        setCustomReader(mojo, className);
        try {
            testGeneratedSwaggerSpecJson();
        } catch (MojoFailureException e) {
            assertEquals(String.format("Cannot load Swagger API reader: %s", className), e.getMessage());
        }
    }

    @Test
    public void testInvalidCustomReaderYaml() throws Exception {
        String className = "com.wordnik.nonexisting.Class";
        setCustomReader(mojo, className);
        try {
            testGeneratedSwaggerSpecJson();
        } catch (MojoFailureException e) {
            assertEquals(String.format("Cannot load Swagger API reader: %s", className), e.getMessage());
        }
    }

    @Test
    public void testGeneratedDoc() throws Exception {
        mojo.execute();

        BufferedReader actualReader = null;
        BufferedReader expectReader = null;
        FileInputStream swaggerJson = null;
        BufferedReader swaggerReader = null;

        try {
            actualReader = new BufferedReader(new InputStreamReader(new FileInputStream(docOutput)));
            expectReader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/sample.html")));

            int count = 0;
            while (true) {
                count++;

                String expect = expectReader.readLine();
                String actual = actualReader.readLine();
                if (expect == null && actual == null) {
                    break;
                }
                Assert.assertEquals(actual.trim(), expect.trim(), "" + count);
            }

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

        List<ApiSource> apisources = (List<ApiSource>) getVariableValueFromObject(mojo, "apiSources");
        ApiSource apiSource = apisources.get(0);
        // Force serialization of example values as json raw values
        apiSource.setJsonExampleValues(true);
        // exclude part of the model when not compliant with jev option (e.g. example expressed as plain string)
        apiSource.setApiModelPropertyExclusions(Collections.singletonList("exclude-when-jev-option-set"));

        mojo.execute();

        // check generated swagger json file
        JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, "swagger.json"));
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/options/jsonExampleValues/expected/swagger.json"));

        JsonNode actualUserNode = actualJson.path("definitions").path("User");
        JsonNode expectUserNode = expectJson.path("definitions").path("User");

        // Cannot test full node equality since tags order is not guaranteed in generated json
        Assert.assertEquals(actualUserNode, expectUserNode);
    }

    @Test
    public void testNullSwaggerOutput() throws Exception {
        List<ApiSource> apisources = (List<ApiSource>) getVariableValueFromObject(mojo, "apiSources");
        apisources.get(0).setSwaggerDirectory(null);
        setVariableValueToObject(mojo, "apiSources", apisources);
        mojo.execute();
        Assert.assertFalse(swaggerOutputDir.exists());
    }

    @Test
    public void testNullMustacheOutput() throws Exception {
        List<ApiSource> apisources = (List<ApiSource>) getVariableValueFromObject(mojo, "apiSources");
        apisources.get(0).setTemplatePath(null);
        setVariableValueToObject(mojo, "apiSources", apisources);
        mojo.execute();
        Assert.assertFalse(docOutput.exists());
    }

    @DataProvider
    private Iterator<Object[]> pathProvider() throws Exception {
        String tempDirPath = createTempDirPath();

        List<Object[]> dataToBeReturned = new ArrayList<Object[]>();
        dataToBeReturned.add(new String[]{tempDirPath + "foo" + File.separator + "bar" + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "bar" + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{"test.html"});

        return dataToBeReturned.iterator();
    }

    @Test(enabled = false, dataProvider = "pathProvider")
    public void testExecuteDirectoryCreated(String path) throws Exception {

        mojo.getApiSources().get(0).setOutputPath(path);

        File file = new File(path);
        mojo.execute();
        Assert.assertTrue(file.exists());
        if (file.getParentFile() != null) {
            FileUtils.deleteDirectory(file.getParentFile());
        }
    }

    @Test
    public void testCustomModelConverterYaml() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.getApiSources().get(0).setModelConverters(ImmutableList.of(PetIdToStringModelConverter.class.getName()));

        assertGeneratedSwaggerSpecYaml("This is a sample.", "/expectedOutput/swagger-with-converter.yaml");
    }

    @Test
    public void testCustomModelConverterJson() throws MojoFailureException, MojoExecutionException, IOException {
        mojo.getApiSources().get(0).setModelConverters(ImmutableList.of(PetIdToStringModelConverter.class.getName()));

        executeAndAssertGeneratedSwaggerSpecJson("This is a sample.", "/expectedOutput/swagger-with-converter.json");
    }

    @Test
    public void testMultipleApiSourcesWithDifferentArtifactName() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/plugin-config-multiple-api-sources.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
        mojo.execute();

        assertTrue(new File(swaggerOutputDir, "custom-file-name-one.json").exists());
        assertGeneratedSwaggerSpecJson("This is a sample.", "/expectedOutput/swagger.json", "custom-file-name-one.json");
        assertTrue(new File(swaggerOutputDir, "custom-file-name-two.json").exists());
        assertGeneratedSwaggerSpecJson("This is a sample.", "/expectedOutput/swagger.json", "custom-file-name-two.json");
        assertTrue(new File(swaggerOutputDir, "swagger.json").exists());
        assertGeneratedSwaggerSpecJson("This is a sample.", "/expectedOutput/swagger.json", "swagger.json");
    }

    private void assertGeneratedSwaggerSpecJson(String description, String expectedOutput, String generatedFileName) throws IOException {
        JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, generatedFileName));
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream(expectedOutput));

        changeDescription(expectJson, description);
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }


    private void executeAndAssertGeneratedSwaggerSpecJson(String description, String expectedOutput) throws MojoExecutionException, MojoFailureException, IOException {
        mojo.execute();
        assertGeneratedSwaggerSpecJson(description, expectedOutput, "swagger.json");
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

    @Test
    public void testFeatureIsAccepted() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/plugin-config.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
        mojo.execute();

        assertTrue(Json.mapper().isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING));
        assertTrue(Json.mapper().isEnabled(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS));
    }

    @Test
    public void testFeatureIsNotFoundFail() throws Exception {
        File testPom = new File(getBasedir(), "src/test/resources/plugin-config-feature-fail.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
        try {
            mojo.execute();
            fail();
        } catch (Exception x) {
            Logger.getAnonymousLogger().log(Level.FINE,x.getMessage(),x);
            assertTrue(Json.mapper().isEnabled(SerializationFeature.WRITE_ENUMS_USING_TO_STRING));
            assertTrue(x.getMessage().contains("com.fasterxml.jackson.core.JsonParser.Feature"));
            assertNotNull(x.getCause());
            assertTrue(x.getCause().getMessage().contains("com.fasterxml.jackson.core.JsonParser.Feature"));
        }
    }



}
