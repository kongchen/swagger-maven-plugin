package com.wordnik.jaxrs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * @author andrewb
 */
@Path("/")
@Api(value = "/")
public class RootPathResource {
    @GET
    @ApiOperation(value = "testingRootPathResource")
    public String testingRootPathResource() {
        return "testingRootPathResource";
    }
}
