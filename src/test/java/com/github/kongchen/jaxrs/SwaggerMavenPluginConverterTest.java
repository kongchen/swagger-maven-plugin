package com.github.kongchen.jaxrs;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import com.github.kongchen.swagger.docgen.mavenplugin.ModelConverterRegistry;
import com.wordnik.swagger.converter.ModelConverters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static com.github.kongchen.TestUtil.deleteDirectory;
import static com.github.kongchen.TestUtil.deleteFile;

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
        deleteDirectory(swaggerOutputDir);
        deleteFile(docOutput);

        File testPom = new File(getBasedir(), "target/test-classes/plugin-config-converter.xml");
        mojo = (ApiDocumentMojo) lookupMojo("generate", testPom);
    }

    @AfterMethod
    public void tearDown() {
        ModelConverterRegistry.clear();
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
