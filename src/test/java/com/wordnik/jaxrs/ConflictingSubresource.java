package com.wordnik.jaxrs;

import com.wordnik.sample.model.Pet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Api(basePath = "/swaggerSubresourcePath", produces = "application/xml", consumes = "application/xml")
public class ConflictingSubresource {

    @GET
    @Path("/{petId}")
    @ApiOperation("")
    public Pet getPetById(
            @ApiParam(name = "id", type = "com.wordnik.sample.model.ListItem", collectionFormat = "list", format = "array")
            @PathParam("petId") Long petId)
    {
        return new Pet();
    }
}
