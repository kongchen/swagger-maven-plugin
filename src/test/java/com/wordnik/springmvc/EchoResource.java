package com.wordnik.springmvc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(value = "/echo", description = "Set of simple endpoints that return whatever value you pass in")
@RequestMapping(value = "/echo", produces = {"application/json", "application/xml"})
public class EchoResource {

    // Tests for @PathVariable
    @RequestMapping(value = "/pathVariableExpectParameterName/{parameterName}", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String pathVariableExpectParameterName(@PathVariable String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/pathVariableExpectVariableName/{parameterName}", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String pathVariableExpectVariableName(@PathVariable(name = "pathVariableName") String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/pathVariableExpectVariableValue/{parameterName}", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String pathVariableExpectVariableValue(@PathVariable(value = "pathVariableValue") String parameterName) {
        return parameterName;
    }

    // Tests for @RequestParam
    @RequestMapping(value = "/requestParamExpectParameterName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestParamExpectParameterName(@RequestParam String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestParamExpectParamName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestParamExpectParamName(@RequestParam(name = "requestParamName") String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestParamExpectParamValue", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestParamExpectParamValue(@RequestParam(value = "requestParamValue") String parameterName) {
        return parameterName;
    }

    // Tests for @RequestHeader
    @RequestMapping(value = "/requestHeaderExpectParameterName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestHeaderExpectParameterName(@RequestHeader String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestHeaderExpectHeaderName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestHeaderExpectHeaderName(@RequestHeader(name = "requestHeaderName") String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestHeaderExpectHeaderValue", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestHeaderExpectHeaderValue(@RequestHeader(value = "requestHeaderValue") String parameterName) {
        return parameterName;
    }

    // Tests for @CookieValue
    @RequestMapping(value = "/cookieValueExpectParameterName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String cookieValueExpectParameterName(@CookieValue String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/cookieValueExpectCookieName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String cookieValueExpectCookieName(@CookieValue(name = "cookieValueName") String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/cookieValueExpectCookieValue", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String cookieValueExpectCookieValue(@CookieValue(value = "cookieValueValue") String parameterName) {
        return parameterName;
    }

    // Tests for @RequestPart
    @RequestMapping(value = "/requestPartExpectParameterName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestPartExpectParameterName(@RequestPart String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestPartExpectPartName", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestPartExpectPartName(@RequestPart(name = "requestPartName") String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestPartExpectPartValue", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "")
    public String requestPartExpectPartValue(@RequestPart(value = "requestPartValue") String parameterName) {
        return parameterName;
    }
}
