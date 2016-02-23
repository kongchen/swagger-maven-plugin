package com.wordnik.springmvc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author andrewb
 */
@RequestMapping(value = "/")
@Api(value = "/")
public class RootPathResource {
    @ApiOperation(value = "testingRootPathResource")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<String> testingRootPathResource() {
        return new ResponseEntity<String>("testingRootPathResource", HttpStatus.OK);
    }
}
