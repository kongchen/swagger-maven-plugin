package com.wordnik.jaxrs;


import com.wordnik.sample.model.Pet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Api(basePath = "/swaggerBasePath", produces = "application/xml", consumes = "application/xml")
@Path("/jaxrsBasePath")
@Produces("application/json")
@Consumes("application/json")
public class ConflictingSwaggerResource {

    /*
    https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X
    http://docs.swagger.io/swagger-core/current/apidocs/index.html?io/swagger/annotations

    // Api                     basePath        produces        consumes
    ApiModel                ???
    ApiModelProperty        dataType        name
    ApiOperation            produces        consumes        response        responseContainer       responseReference
    // ApiParam                format          name            type            collectionFormat
    ApiResponse             reference       response        responseContainer
    ApiResponses            ---
    ResponseHeader          ---
     */

    @GET
    @Path("/{petId}")
    @ApiOperation(value = "", produces = "application/xml", consumes = "application/xml", httpMethod = "put",
                  response = String.class, responseContainer = "list", responseReference = "#/definitions/ListItem")
    public Pet getPetById(
            @ApiParam(name = "id", type = "com.wordnik.sample.model.ListItem", collectionFormat = "list", format = "array")
            @PathParam("petId") Long petId)
    {
        return new Pet();
    }

    @POST
    @Path("/bean")
    @ApiOperation("")
    public Pet createBean(@BeanParam ConflictingBean conflictingBean) {
        return new Pet();
    }

    @Path("/subresource")
    @ApiOperation("")
    public ConflictingSubresource subresourceEndpoint() {
        return new ConflictingSubresource();
    }

    @GET
    @Path("/stringList")
    @ApiOperation(value = "", response = String.class, responseContainer = "list")
    public Response getStringList()
    {
        return Response.status(200).build();
    }
}
