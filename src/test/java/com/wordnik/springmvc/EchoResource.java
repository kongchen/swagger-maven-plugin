package com.wordnik.springmvc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import javax.ws.rs.core.MediaType;

@Api(value = "/echo", description = "Set of simple endpoints that return whatever value you pass in")
@RequestMapping(value = "/echo", produces = {MediaType.APPLICATION_JSON, "application/xml"})
public class EchoResource {

    // Tests for @PathVariable
    @RequestMapping(value = "/pathVariableExpectParameterName/{parameterName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String pathVariableExpectParameterName(@PathVariable final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/pathVariableExpectVariableName/{parameterName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String pathVariableExpectVariableName(@PathVariable(name = "pathVariableName") final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/pathVariableExpectVariableValue/{parameterName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String pathVariableExpectVariableValue(@PathVariable(value = "pathVariableValue") final String parameterName) {
        return parameterName;
    }

    // Tests for @RequestParam
    @RequestMapping(value = "/requestParamExpectParameterName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestParamExpectParameterName(@RequestParam final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestParamExpectParamName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestParamExpectParamName(@RequestParam(name = "requestParamName") final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestParamExpectParamValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestParamExpectParamValue(@RequestParam(value = "requestParamValue") final String parameterName) {
        return parameterName;
    }

    // Tests for @RequestHeader
    @RequestMapping(value = "/requestHeaderExpectParameterName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestHeaderExpectParameterName(@RequestHeader final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestHeaderExpectHeaderName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestHeaderExpectHeaderName(@RequestHeader(name = "requestHeaderName") final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestHeaderExpectHeaderValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestHeaderExpectHeaderValue(@RequestHeader(value = "requestHeaderValue") final String parameterName) {
        return parameterName;
    }

    // Tests for @CookieValue
    @RequestMapping(value = "/cookieValueExpectParameterName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String cookieValueExpectParameterName(@CookieValue final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/cookieValueExpectCookieName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String cookieValueExpectCookieName(@CookieValue(name = "cookieValueName") final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/cookieValueExpectCookieValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String cookieValueExpectCookieValue(@CookieValue(value = "cookieValueValue") final String parameterName) {
        return parameterName;
    }

    // Tests for @RequestPart
    @RequestMapping(value = "/requestPartExpectParameterName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestPartExpectParameterName(@RequestPart final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestPartExpectPartName", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestPartExpectPartName(@RequestPart(name = "requestPartName") final String parameterName) {
        return parameterName;
    }

    @RequestMapping(value = "/requestPartExpectPartValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "")
    public String requestPartExpectPartValue(@RequestPart(value = "requestPartValue") final String parameterName) {
        return parameterName;
    }
}
