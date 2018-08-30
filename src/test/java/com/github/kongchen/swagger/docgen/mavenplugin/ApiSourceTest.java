package com.github.kongchen.swagger.docgen.mavenplugin;

import com.google.common.collect.Sets;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.models.Info;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApiSourceTest {

    @Spy
    protected ApiSource apiSource;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.initMocks(this);
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

        Mockito.when(apiSource.getValidClasses(SwaggerDefinition.class)).thenReturn(validClasses);
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
