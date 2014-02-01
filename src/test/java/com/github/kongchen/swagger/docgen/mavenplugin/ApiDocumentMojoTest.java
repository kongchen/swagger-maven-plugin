package com.github.kongchen.swagger.docgen.mavenplugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/4/13
 */
public class ApiDocumentMojoTest {

    ApiDocumentMojo mojo = new ApiDocumentMojo();

    String tmpSwaggerOutputDir = "apidocsf";

    ApiSource apiSource;

    @BeforeMethod
    private void prepare() {
        List<ApiSource> apiSources = new ArrayList<ApiSource>();
        apiSource = new ApiSource();
        apiSource.setApiVersion("1.0");
        apiSource.setBasePath("http://example.com");
        apiSource.setLocations("sample.api");
        apiSource.setOutputPath("temp.html");
        apiSource.setOutputTemplate("https://raw2.github.com/kongchen/api-doc-template/master/v1.1/strapdown.html.mustache");
        apiSource.setSwaggerDirectory(tmpSwaggerOutputDir);

        apiSources.add(apiSource);
        mojo.setApiSources(apiSources);
    }

    @AfterMethod
    private void fin() throws IOException {
        File tempOutput = new File(tmpSwaggerOutputDir);
//        FileUtils.deleteDirectory(tempOutput);
    }

    /**
     * {
     * "apiVersion" : "1.0",
     * "swaggerVersion" : "1.1",
     * "basePath" : "http://localhost/apidocsf",
     * "apis" : [ {
     * "path" : "/v2_car.{format}",
     * "description" : "Operations about cars"
     * }, {
     * "path" : "/garage.{format}",
     * "description" : "Operations about garages"
     * }, {
     * "path" : "/car.{format}",
     * "description" : "Operations about cars"
     * } ]
     * }
     */
    @Test
    public void testSwaggerOutputFlat() throws IOException, MojoFailureException, MojoExecutionException {
        apiSource.setSwaggerDirectory(tmpSwaggerOutputDir);
        apiSource.setUseOutputFlatStructure(true);

        File output = new File(tmpSwaggerOutputDir);
        FileUtils.deleteDirectory(output);

        mojo.execute();
        List<String> flatfiles = new ArrayList<String>();

        Collections.addAll(flatfiles, output.list());
        Collections.sort(flatfiles);
        Assert.assertEquals(flatfiles.get(0), "car.json");
        Assert.assertEquals(flatfiles.get(1), "garage.json");
        Assert.assertEquals(flatfiles.get(2), "service.json");
        Assert.assertEquals(flatfiles.get(3), "v2_car.json");

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(FileUtils.readFileToByteArray(new File(output, "service.json")));
        JsonNode apis = node.get("apis");
        Assert.assertEquals(apis.size(), 3);
        List<String> pathInService = new ArrayList<String> ();
        for (JsonNode api : apis) {
            pathInService.add(api.get("path").asText());
        }
        Collections.sort(pathInService);
        Assert.assertEquals(pathInService.get(0), "/car.{format}");
        Assert.assertEquals(pathInService.get(1), "/garage.{format}");
        Assert.assertEquals(pathInService.get(2), "/v2_car.{format}");
    }

    @Test
    public void testSwaggerOutput() throws IOException, MojoFailureException, MojoExecutionException {
        apiSource.setSwaggerDirectory(tmpSwaggerOutputDir);
        apiSource.setUseOutputFlatStructure(false);

        File output = new File(tmpSwaggerOutputDir);
        FileUtils.deleteDirectory(output);

        mojo.execute();
        List<File> outputFiles = new ArrayList<File>();

        Collections.addAll(outputFiles, output.listFiles());
        Collections.sort(outputFiles);
        Assert.assertEquals(outputFiles.get(0).getName(), "car.json");
        Assert.assertEquals(outputFiles.get(1).getName(), "garage.json");
        Assert.assertEquals(outputFiles.get(2).getName(), "service.json");
        Assert.assertEquals(outputFiles.get(3).getName(), "v2");
        File v2 = outputFiles.get(3);
        Assert.assertTrue(v2.isDirectory());
        String[] v2carfile = v2.list();
        Assert.assertEquals(v2carfile[0], "car.json");


        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node = objectMapper.readTree(FileUtils.readFileToByteArray(new File(output, "service.json")));
        JsonNode apis = node.get("apis");
        Assert.assertEquals(apis.size(), 3);
        List<String> pathInService = new ArrayList<String> ();
        for (JsonNode api : apis) {
            pathInService.add(api.get("path").asText());
        }
        Collections.sort(pathInService);
        Assert.assertEquals(pathInService.get(0), "/car.{format}");
        Assert.assertEquals(pathInService.get(1), "/garage.{format}");
        Assert.assertEquals(pathInService.get(2), "/v2/car.{format}");
    }

    @Test(enabled = true)
    public void testExecute() throws Exception {
        mojo.execute();
        FileInputStream testOutputIs = new FileInputStream(new File("temp.html"));
        InputStream expectIs = this.getClass().getResourceAsStream("/sample.html");
        int count = 0;
        while (true) {
            count++;
            int expect = expectIs.read();
            int actual = testOutputIs.read();

            Assert.assertEquals( expect, actual, ""+count);
            if (expect == -1) {
                break;
            }
        }
    }

    @DataProvider
    private Iterator<String[]> pathProvider() throws Exception {
        String tempDirPath = createTempDirPath();

        List<String[]> dataToBeReturned = new ArrayList<String[]>();
        dataToBeReturned.add(new String[]{tempDirPath + "foo" + File.separator + "bar" + File
                .separator + "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "bar" + File.separator +
                "test.html"});
        dataToBeReturned.add(new String[]{tempDirPath + File.separator + "test.html"});
        dataToBeReturned.add(new String[]{"test.html"});

        return dataToBeReturned.iterator();
    }

    @Test(dataProvider = "pathProvider")
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
}
