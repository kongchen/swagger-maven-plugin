package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.reader.SpringMvcApiReader;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.*;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;


public class SpringMVCResponseStatusTest {

    private static final String REASON = "reason";
    private static final String SUCCESSFUL_OPERATION_DESCRIPTION = "successful operation";
    private static final String ACCEPTED_OPERATION_DESCRIPTION = "202 message";
    private static final String CONFLICT_OPERATION_DESCRIPTION = "conflict message";
    private static final Model RETURN_TYPE_STRING = new ModelImpl().type("string");
    private static final Model RETURN_TYPE_INTEGER = new ModelImpl().type("integer").format("int32");


    private List<SwaggerExtension> extensions = SwaggerExtensions.getExtensions();
    private SpringMvcApiReader reader;

    @BeforeMethod
    public void setUp() {
        reader = new SpringMvcApiReader(new Swagger(), new SystemStreamLog());
    }

    @AfterMethod
    public void resetExtenstions() {
        SwaggerExtensions.setExtensions(extensions);
    }

    @Test
    public void testStatusCommon() throws GenerateException {
        testMethod("/getString", HttpMethod.GET,
              ImmutableMap.of(
                    HttpStatus.OK,
                    new Response().description(SUCCESSFUL_OPERATION_DESCRIPTION).responseSchema(RETURN_TYPE_STRING))
        );
    }


    @Test
    public void testStatusOKOverridden() throws GenerateException {
        testMethod("/getString_200", HttpMethod.GET,
              ImmutableMap.of(
                    HttpStatus.OK,
                    new Response().description(REASON).responseSchema(RETURN_TYPE_STRING))
        );
    }


    @Test
    public void testAPIResponse() throws GenerateException {
        testMethod("/getString_ApiResponse_202_409", HttpMethod.GET,
              ImmutableMap.of(
                    HttpStatus.ACCEPTED,
                    new Response().description(ACCEPTED_OPERATION_DESCRIPTION).responseSchema(RETURN_TYPE_STRING),
                    HttpStatus.CONFLICT,
                    new Response().description(CONFLICT_OPERATION_DESCRIPTION))
        );
    }

    @Test
    public void testAPIResponseOverridden() throws GenerateException {
        testMethod("/getString_ApiResponse_202_409_over", HttpMethod.GET,
              ImmutableMap.of(
                    HttpStatus.ACCEPTED,
                    new Response().description(ACCEPTED_OPERATION_DESCRIPTION).responseSchema(RETURN_TYPE_INTEGER),
                    HttpStatus.CONFLICT,
                    new Response().description(CONFLICT_OPERATION_DESCRIPTION))
        );
    }


    @Test
    public void testStatusCreatedOverridden() throws GenerateException {
        testMethod("/getString_201", HttpMethod.POST,
              ImmutableMap.of(
                    HttpStatus.CREATED,
                    new Response().description(SUCCESSFUL_OPERATION_DESCRIPTION).responseSchema(RETURN_TYPE_INTEGER))
        );
    }

    private void testMethod(String url, HttpMethod method, Map<HttpStatus, Response> expectedResults) throws GenerateException
    {
        Swagger result = reader.read(Collections.singleton(TestController.class));

        Map<String, Response> responseMap = result.getPaths().get(url).getOperationMap().get(method).getResponses();
        Assert.assertEquals(responseMap.size(), expectedResults.size());

        for (Map.Entry<HttpStatus, Response> expectedResult : expectedResults.entrySet())
        {
            Response response = responseMap.get(expectedResult.getKey().toString());
            Assert.assertNotNull(response);
            Assert.assertEquals(response.getDescription(), expectedResult.getValue().getDescription());
            Assert.assertEquals(response.getResponseSchema(), expectedResult.getValue().getResponseSchema());
        }
    }

    @RestController
    private static class TestController {

        @GetMapping("/getString")
        public String getString() {
            return "";
        }

        @GetMapping("/getString_200")
        @ResponseStatus(value = HttpStatus.OK, reason = REASON)
        public String getStringWithStatus() {
            return "";
        }

        @PostMapping("/getString_201")
        @ResponseStatus(HttpStatus.CREATED)
        public Integer postAndGetStatus() {
            return 0;
        }

        @GetMapping("/getString_ApiResponse_202_409")
        @ApiResponses({@ApiResponse(code = 202, message = ACCEPTED_OPERATION_DESCRIPTION, response = String.class), @ApiResponse(code = 409, message = CONFLICT_OPERATION_DESCRIPTION)})
        public String getStringAPIResponse() {
            return "";
        }

        @ResponseStatus(code = HttpStatus.ACCEPTED)
        @GetMapping("/getString_ApiResponse_202_409_over")
        @ApiResponses({@ApiResponse(code = 202, message = ACCEPTED_OPERATION_DESCRIPTION, response = Integer.class), @ApiResponse(code = 409, message = CONFLICT_OPERATION_DESCRIPTION)})
        public Integer getStringAPIResponseOverridden() {
            return 0;
        }
    }
}
