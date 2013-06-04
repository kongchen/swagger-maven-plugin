package com.github.kongchen.swagger.docgen.mavenplugin;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/4/13
 */
public class ApiDocumentMojoTest {

    private List<ApiSource> apiSources = new ArrayList<ApiSource>();
    ApiDocumentMojo mojo = new ApiDocumentMojo();

    @BeforeClass
    private void prepare(){

        ApiSource apiSource;
        apiSource = new ApiSource();
        apiSource.setApiVersion("1.0");
        apiSource.setBasePath("http://example.com");
        apiSource.setLocations("sample.api");
        apiSource.setOutputPath("temp.html");
        apiSource.setOutputTemplate("strapdown.html.mustache");
        apiSource.setSwaggerDirectory(null);

        apiSources.add(apiSource);
        mojo.setApiSources(apiSources);

    }
    @Test
    public void testExecute() throws Exception {
        mojo.execute();
    }
}
