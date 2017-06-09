package com.github.kongchen.swagger.docgen.mavenplugin;

import com.google.common.collect.Sets;
import io.swagger.annotations.Api;
import java.util.Collections;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.springframework.web.bind.annotation.RestController;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SpringMavenDocumentSourceTest
{
    @Test
    public void testGetValidClasses() throws Exception
    {
        Log log = new SystemStreamLog();

        ApiSource apiSource = new ApiSource();
        apiSource.setLocations(Collections.singletonList(this.getClass().getPackage().getName()));
        apiSource.setSwaggerDirectory("./");

        SpringMavenDocumentSource springMavenDocumentSource = new SpringMavenDocumentSource(apiSource, log, "UTF-8");

        Sets.SetView<Class<?>> validClasses = springMavenDocumentSource.getValidClasses();

        Assert.assertEquals(validClasses.size(), 2);
        Assert.assertTrue(validClasses.contains(ExampleController1.class));
        Assert.assertTrue(validClasses.contains(ExampleController2.class));
    }


    @RestController
    private static class ExampleController1
    {
    }

    @Api
    @RestController
    private static class ExampleController2
    {
    }
}
