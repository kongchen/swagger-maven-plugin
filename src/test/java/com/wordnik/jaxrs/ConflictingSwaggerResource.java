package com.wordnik.jaxrs;

import com.wordnik.sample.model.Pet;
import io.swagger.annotations.*;

import javax.ws.rs.*;

@Api
@Path("/jaxrsBasePath")
@Produces("application/json")
@Consumes("application/json")
public class ConflictingSwaggerResource {

    @GET
    @Path("/{petId}")
    @ApiOperation("")
    public Pet getPetById(@PathParam("petId") Long petId)
    {
        return new Pet();
    }
}
