package com.wordnik.jaxrs;

import com.wordnik.sample.model.Pet;
import io.swagger.annotations.*;

import javax.ws.rs.*;

@Api(basePath = "/swaggerBasePath", produces = "application/xml", consumes = "application/xml")
@Path("/jaxrsBasePath")
@Produces("application/json")
@Consumes("application/json")
public class ConflictingSwaggerResource {

    @GET
    @Path("/{petId}")
    @ApiOperation("")
    public Pet getPetById(
            @ApiParam(name = "id", type = "com.wordnik.sample.model.ListItem", collectionFormat = "list", format = "array")
            @PathParam("petId") Long petId)
    {
        return new Pet();
    }

    // TODO Test subresource and @BeanParam
}
