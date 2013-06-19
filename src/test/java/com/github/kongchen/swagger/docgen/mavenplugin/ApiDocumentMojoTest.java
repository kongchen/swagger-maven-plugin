package com.github.kongchen.swagger.docgen.mavenplugin;

import static junit.framework.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/4/13
 */
public class ApiDocumentMojoTest {

    private List<ApiSource> apiSources = new ArrayList<ApiSource>();
    ApiDocumentMojo mojo = new ApiDocumentMojo();
    String outputPath;
    String tempDirPath;

    @BeforeClass
    private void prepare() throws Exception {
        tempDirPath = createTempDirPath();
        outputPath = tempDirPath + "/test.html";

        ApiSource apiSource;
        apiSource = new ApiSource();
        apiSource.setCreateOutputDirectories(true);
        apiSource.setApiVersion("1.0");
        apiSource.setBasePath("http://example.com");
        apiSource.setLocations("sample.api");
        apiSource.setOutputPath(outputPath);
        apiSource.setOutputTemplate("strapdown.html.mustache");
        apiSource.setWithFormatSuffix(false);
        apiSource.setSwaggerDirectory("apidocsf");

        apiSources.add(apiSource);
        mojo.setApiSources(apiSources);

    }
    @Test(enabled = false)
    public void testExecute() throws Exception {
        mojo.execute();
        FileInputStream testOutputIs = new FileInputStream(new File("temp.html"));
        InputStream expectIs = this.getClass().getResourceAsStream("/sample.html");
        while (true) {
            int expect = expectIs.read();
            int actual = testOutputIs.read();

            Assert.assertEquals(expect, actual);
            if (expect == -1) {
                break;
            }
        }
    }

    @Test(enabled = true)
    public void testExecuteDirectoryCreated() throws Exception {
        mojo.execute();
        File file = new File(outputPath);
        assertTrue(file.exists());
        FileUtils.deleteDirectory(new File(tempDirPath));
    }

    private String createTempDirPath() throws Exception {
        File tempFile = File.createTempFile("swagmvn", "test");
        String path = tempFile.getAbsolutePath();
        tempFile.delete();
        return path;
    }
}
