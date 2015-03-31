package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.mustache.MustacheApi;
import com.github.kongchen.swagger.docgen.mustache.MustacheDocument;
import com.github.kongchen.swagger.docgen.mustache.MustacheOperation;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.testng.Assert;
import org.testng.annotations.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class SpringMavenDocumentSourceTest {

    private ApiSource prepareSpring() {
        ApiSource apiSource = new ApiSource();
        apiSource.setApiVersion("1.0");
        apiSource.setBasePath("http://example.com");
        apiSource.setLocations("com.github.kongchen.springmvc.controller");
        apiSource.setOutputPath("sample.html");
        apiSource.setOutputTemplate("https://github.com/kongchen/api-doc-template/blob/master/v1.1/html.mustache");
        apiSource.setSwaggerDirectory(null);
        apiSource.setSupportSpringMvc(true);
        return apiSource;
    }

    @Test
    public void testSpring() throws Exception {
        ApiSource apiSource = prepareSpring();
        AbstractDocumentSource documentSource = new SpringMavenDocumentSource(apiSource, new SystemStreamLog());
        documentSource.loadOverridingModels();
        documentSource.loadDocuments();
        OutputTemplate outputTemplate = new OutputTemplate(documentSource);
        assertEquals(apiSource.getApiVersion(), outputTemplate.getApiVersion());
        assertEquals(3, outputTemplate.getApiDocuments().size());
        for (MustacheDocument doc : outputTemplate.getApiDocuments()) {
            if (doc.getIndex() == 1) {
                Assert.assertEquals(doc.getResourcePath(), "/car");
                for (MustacheApi api : doc.getApis()) {
                    assertTrue(api.getUrl().startsWith(apiSource.getBasePath()));
                    assertFalse(api.getPath().contains("{format}"));
                    for (MustacheOperation op : api.getOperations()) {
                        if (op.getOpIndex() == 2) {

                            Assert.assertEquals(op.getParameters().size(), 3);

                            //ImplicitParams are not supported
//                            Assert.assertEquals("ETag", op.getResponseHeader().getParas().get(0).getName());

                            Assert.assertEquals("carId",
                                    op.getRequestPath().getParas().get(0).getName());
                            Assert.assertEquals("1.0 to 10.0",
                                    op.getRequestPath().getParas().get(0).getAllowableValue());

                            Assert.assertEquals("e",
                                    op.getRequestQuery().getParas().get(0).getName());

                            Assert.assertEquals("Accept",
                                    op.getRequestHeader().getParas().get(0).getName());
                            Assert.assertEquals("MediaType",
                                    op.getRequestHeader().getParas().get(0).getType());
                            Assert.assertEquals("application/json, application/*",
                                    op.getRequestHeader().getParas().get(0).getAllowableValue());
                            Assert.assertEquals(op.getResponseMessages().size(), 2);
                            Assert.assertEquals(op.getResponseMessages().get(0).getMessage(), "Invalid ID supplied");
                            Assert.assertEquals(op.getResponseMessages().get(0).getCode(), 400);
                            Assert.assertEquals(op.getResponseMessages().get(1).getCode(), 404);
                            // Testing deprecated method. Should remove tests when deprecated method is gone
                            Assert.assertEquals(op.getErrorResponses().size(), 2);
                            Assert.assertEquals(op.getErrorResponses().get(0).getMessage(), "Invalid ID supplied");
                            Assert.assertEquals(op.getErrorResponses().get(0).getCode(), 400);
                            Assert.assertEquals(op.getErrorResponses().get(1).getCode(), 404);
                            Assert.assertEquals(op.getAuthorizations().get(0).getType(), "oauth2");
                            Assert.assertEquals(op.getAuthorizations().get(0).getAuthorizationScopes().get(0).description(), "car1 des get");
                        }
                        if (op.getOpIndex() == 1) {
                            Assert.assertEquals(op.getSummary(), "search cars");
                        }
                    }
                }
            }
            if (doc.getIndex() == 2) {
                Assert.assertEquals(doc.getResourcePath(), "/v2/car");
            }
            if (doc.getIndex() == 3) {
                Assert.assertEquals(doc.getResourcePath(), "/garage");
            }

        }
    }


}