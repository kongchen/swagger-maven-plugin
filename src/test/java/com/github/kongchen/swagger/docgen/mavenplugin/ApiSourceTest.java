package com.github.kongchen.swagger.docgen.mavenplugin;

import com.google.common.collect.Sets;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class ApiSourceTest {

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetExternalDocsNoneFound() {
        // given
        @SwaggerDefinition
        class TestClassNoExternalDocs { }

        ApiSource apiSource = spy(ApiSource.class);
        when(apiSource.getValidClasses(SwaggerDefinition.class)).thenReturn(Sets.newHashSet(TestClassNoExternalDocs.class));

        // when
        ExternalDocs externalDocs = apiSource.getExternalDocs();

        // then
        Assert.assertNull(externalDocs);
    }

    @Test
    public void testGetExternalDocsFound() {
        // given
        @SwaggerDefinition(externalDocs = @io.swagger.annotations.ExternalDocs(value = "Example external docs", url = "https://example.com/docs"))
        class TestClassExternalDocs { }

        ApiSource apiSource = spy(ApiSource.class);
        when(apiSource.getValidClasses(SwaggerDefinition.class)).thenReturn(Sets.newHashSet(TestClassExternalDocs.class));

        // when
        ExternalDocs externalDocs = apiSource.getExternalDocs();

        // then
        Assert.assertNotNull(externalDocs);
        Assert.assertEquals(externalDocs.getDescription(), "Example external docs");
        Assert.assertEquals(externalDocs.getUrl(), "https://example.com/docs");
    }

    @Test
    public void testGetInfo0VendorExtensions() {
        Map<String, Object> logo = new HashMap<String, Object>();
        logo.put("logo", "logo url");
        logo.put("description", "This is our logo.");

        Map<String, Object> website = new HashMap<String, Object>();
        website.put("website", "website url");
        website.put("description", "This is our website.");

        Map<String, Object> expectedExtensions = new HashMap<String, Object>();
        expectedExtensions.put("x-logo", logo);
        expectedExtensions.put("x-website", website);


        Set<Class<?>> validClasses = Sets.newHashSet(ApiSourceTestClass.class);
        ApiSource apiSource = spy(ApiSource.class);

        when(apiSource.getValidClasses(SwaggerDefinition.class)).thenReturn(validClasses);
        Info info = apiSource.getInfo();

        Map<String, Object> vendorExtensions = info.getVendorExtensions();
        Assert.assertEquals(vendorExtensions.size(), 2);
        Assert.assertEquals(vendorExtensions, expectedExtensions);
    }

    @SwaggerDefinition(info = @io.swagger.annotations.Info(
            title = "ApiSourceTestClass",
            version = "1.0.0",
            extensions = {
                    @Extension(name = "logo", properties = {
                            @ExtensionProperty(name = "logo", value = "logo url"),
                            @ExtensionProperty(name = "description", value = "This is our logo.")
                    }),
                    @Extension(name = "website", properties = {
                            @ExtensionProperty(name = "website", value = "website url"),
                            @ExtensionProperty(name = "description", value = "This is our website.")
                    })
            }
    ))
    private static class ApiSourceTestClass {

    }
}
