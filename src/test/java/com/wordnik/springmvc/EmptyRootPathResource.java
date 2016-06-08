package com.wordnik.springmvc;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author carlosjgp
 */
@RequestMapping
@Api
public class EmptyRootPathResource {
    @ApiOperation(value = "testingEmptyRootPathResource")
    @RequestMapping(value="/testingEmptyRootPathResource",method = RequestMethod.GET)
    public ResponseEntity<String> testingEmptyRootPathResource() {
        return new ResponseEntity<String>("testingEmptyRootPathResource", HttpStatus.OK);
    }
}
