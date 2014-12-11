package com.github.kongchen.swagger.docgen.remote;

import static org.testng.Assert.*;

import java.net.URI;

import com.github.kongchen.swagger.docgen.LogAdapter;
import org.apache.log4j.Logger;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RemoteDocumentSourceTest {

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(enabled = false)
    public void testLoadDocuments() throws Exception {
        RemoteDocumentSource remoteDocumentSource = new RemoteDocumentSource(
                new LogAdapter(Logger.getLogger("test")),
                URI.create("http://petstore.swagger.wordnik.com/api/api-docs"),
//                "/home/chekong/workspace/kongchen/swagger-maven-example/templates/strapdown.html.mustache",
                "/Users/kongchen/workspace/swagger-maven-example/templates/strapdown.html.mustache",
                "/tmp/output.html",
                "/tmp", null, false, null, null);
        remoteDocumentSource.loadDocuments();

        remoteDocumentSource.toDocuments();
        remoteDocumentSource.toSwaggerDocuments(null);
    }

    @Test
    public void testWithFormatSuffix() throws Exception {

    }
}