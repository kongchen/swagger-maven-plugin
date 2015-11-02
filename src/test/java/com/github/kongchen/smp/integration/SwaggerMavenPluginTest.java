package com.github.kongchen.smp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import io.swagger.util.Json;
import net.javacrumbs.jsonunit.core.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.jettison.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import static net.javacrumbs.jsonunit.JsonAssert.assertJsonEquals;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;

/**
 * Created by chekong on 8/15/14.
 */
public class SwaggerMavenPluginTest extends AbstractMojoTestCase {

    private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui");
    private File docOutput = new File(getBasedir(), "generated/document.html");
    private ApiDocumentMojo mojo;

    @BeforeMethod
    protected void setUp() throws Exception {
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

    @Test
    public void testGeneratedSwaggerSpecJson() throws Exception {
        mojo.execute();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, "swagger.json"));
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/expectedOutput/swagger.json"));
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

    @Test
    public void testGeneratedSwaggerSpecYaml() throws Exception {
        mojo.getApiSources().get(0).setOutputFormat("yaml");
        mojo.execute();

        String actualYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(FileUtils.readFileToString(new File(swaggerOutputDir, "swagger.yaml"))));
        String expectYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(this.getClass().getResourceAsStream("/expectedOutput/swagger.yaml")));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(YamlToJson(actualYaml));
        JsonNode expectJson = mapper.readTree(YamlToJson(expectYaml));
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
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
                if (expect == null && actual == null)
                    break;
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
            if (actualReader != null) actualReader.close();
            if (expectReader != null) expectReader.close();
            if (swaggerJson != null) swaggerJson.close();
            if (swaggerReader != null) swaggerReader.close();
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
        ObjectMapper mapper = new ObjectMapper();
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
    private Iterator<String[]> pathProvider() throws Exception {
        String tempDirPath = createTempDirPath();

        List<String[]> dataToBeReturned = new ArrayList<String[]>();
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

    private String createTempDirPath() throws Exception {
        File tempFile = File.createTempFile("swagmvn", "test");
        String path = tempFile.getAbsolutePath();
        tempFile.delete();
        return path;
    }

    private String YamlToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);
        return new JSONObject(map).toString();
    }

}
