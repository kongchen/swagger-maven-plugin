package com.wordnik.stringwrapper;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;

@Api
@RequestMapping(value = "/wrappers", produces = {"application/json"})
public class SimpleWrappersService {

    @RequestMapping(method = RequestMethod.POST, value = "/body")
    public String stringWrapperBody(
            @RequestBody @ApiParam(value = "Must be passed as JSON object", required = true) SimpleStringWrapper wrapper) {
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/param")
    public String stringWrapperParam(
            @RequestParam @ApiParam(value = "Must be passed as String", required = true) SimpleStringWrapper wrapper) {
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/header")
    public String stringWrapperHeader(
            @RequestHeader @ApiParam(value = "Must be passed as String", required = true) SimpleStringWrapper wrapper) {
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/path/{wrapper}")
    public String stringWrapperPath(
            @PathVariable @ApiParam(value = "Must be passed as String", required = true) SimpleStringWrapper wrapper) {
        return null;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/cookie")
    public String stringWrapperCookie(
            @CookieValue @ApiParam(value = "Must be passed as String", required = true) SimpleStringWrapper wrapper) {
        return null;
    }
}
