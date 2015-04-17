package com.github.kongchen.jaxrs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author chekong
 */
public class SwaggerMavenPluginConverterTest extends AbstractMojoTestCase {

    private File swaggerOutputDir = new File(getBasedir(), "generated/swagger-ui");
    private File docOutput = new File(getBasedir(), "generated/document-converter.html");
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

        File testPom = new File(getBasedir(), "target/test-classes/plugin-config-converter.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
    }

    @Test
    public void testGeneratedDoc() throws Exception {
        mojo.execute();

        final InputStream resource = getClass().getResourceAsStream("/converter-sample.html");
        final List<String> expect = IOUtils.readLines(resource);
        final List<String> testOutput = FileUtils.readLines(docOutput);

        Assert.assertEquals(expect.size(), testOutput.size());
        for (int i = 0; i < expect.size(); i++) {
            Assert.assertEquals(expect.get(i), testOutput.get(i));
        }
    }

}
