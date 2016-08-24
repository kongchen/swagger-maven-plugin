package com.github.kongchen.smp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.github.kongchen.smp.integration.utils.TestUtils.YamlToJson;
import static com.github.kongchen.smp.integration.utils.TestUtils.changeDescription;
import static com.github.kongchen.smp.integration.utils.TestUtils.createTempDirPath;
import static com.github.kongchen.smp.integration.utils.TestUtils.setCustomReader;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
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
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

/**
 * @author chekong on 8/15/14.
 */
public class SpringMvcTest extends AbstractMojoTestCase {
    private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui-spring");
    private File docOutput = new File(getBasedir(), "generated/document-spring.html");
    private ApiDocumentMojo mojo;

    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();

        try {
            FileUtils.deleteDirectory(swaggerOutputDir);
            FileUtils.forceDelete(docOutput);
        } catch (Exception e) {
            //ignore
        }

        File testPom = new File(getBasedir(), "target/test-classes/plugin-config-springmvc.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
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
            File actual = docOutput;
            File expected = new File(this.getClass().getResource("/sample-springmvc.html").getFile());
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
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/options/jsonExampleValues/expected/swagger-spring.json"));

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

    private void assertGeneratedSwaggerSpecJson(String description) throws MojoExecutionException, MojoFailureException, IOException {
        mojo.execute();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(new File(swaggerOutputDir, "swagger.json"));
        JsonNode expectJson = mapper.readTree(this.getClass().getResourceAsStream("/expectedOutput/swagger-spring.json"));

        changeDescription(expectJson, description);
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

    private void assertGeneratedSwaggerSpecYaml(String description) throws MojoExecutionException, MojoFailureException, IOException {
        mojo.getApiSources().get(0).setOutputFormats("yaml");
        mojo.execute();

        String actualYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(FileUtils.readFileToString(new File(swaggerOutputDir, "swagger.yaml"))));
        String expectYaml = io.swagger.util.Yaml.pretty().writeValueAsString(
                new Yaml().load(this.getClass().getResourceAsStream("/expectedOutput/swagger-spring.yaml")));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualJson = mapper.readTree(YamlToJson(actualYaml));
        JsonNode expectJson = mapper.readTree(YamlToJson(expectYaml));

        changeDescription(expectJson, description);
        assertJsonEquals(expectJson, actualJson, Configuration.empty().when(IGNORING_ARRAY_ORDER));
    }

}
