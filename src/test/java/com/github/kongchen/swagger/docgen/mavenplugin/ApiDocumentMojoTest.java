package com.github.kongchen.swagger.docgen.mavenplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/4/13
 */
public class ApiDocumentMojoTest {

    private List<ApiSource> apiSources = new ArrayList<ApiSource>();

    ApiDocumentMojo mojo = new ApiDocumentMojo();

    @BeforeClass
    private void prepare() {

        ApiSource apiSource;
        apiSource = new ApiSource();
        apiSource.setApiVersion("1.0");
        apiSource.setBasePath("http://example.com");
        apiSource.setLocations("sample.api");
        apiSource.setOutputPath("temp.html");
        apiSource.setOutputTemplate("https://raw.github.com/kongchen/api-doc-template/master/v1.1/strapdown.html" +
                ".mustache");
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
