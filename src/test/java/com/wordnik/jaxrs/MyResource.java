package com.wordnik.jaxrs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.wordnik.sample.model.Pet;

@Api(description = "Operations about pets")
@Produces({"application/json", "application/xml"})
public interface MyResource {

	//contrived example test case for swagger-maven-plugin issue #358
	@GET
    @ApiOperation(value = "Find pet(s) by ID",
            notes = "This is a contrived example",
            response = Pet.class
    )
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found")})
	public abstract Response getPetsById(
            @ApiParam(value = "start ID of pets that need to be fetched", allowableValues = "range[1,99]", required = true) 
            @QueryParam("startId") Long startId,
            @ApiParam(value = "end ID of pets that need to be fetched", allowableValues = "range[1,99]", required = true) 
            @QueryParam("endId") Long endId)
			throws com.wordnik.sample.exception.NotFoundException;

}