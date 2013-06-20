package sample.api.car;

import com.sun.jersey.api.NotFoundException;
import com.wordnik.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 5/13/13
 */
@Path("/car.json")
@Api(value = "/car", description = "Operations about cars")
@Produces({"application/json"})
public class CarResourceV1 {
    @GET
    @Path("/{carId}")
    @ApiOperation(value = "Find car by ID", notes = "To get car info by car's Id",
            responseClass = "sample.model.Car")
    @ApiErrors(value = {@ApiError(code = 400, reason = "Invalid ID supplied"),
            @ApiError(code = 404, reason = "Car not found")})
    @ApiParamsImplicit(value = {@ApiParamImplicit(name = "ETag", paramType = "response_header", value = "version", dataType = "string")})
    public Response getCarById(
            @ApiParam(value = "ID of car that needs to be fetched", allowableValues = "range[1,10]",
                    required = true) @PathParam("carId") String carId,
            @ApiParam()
            @HeaderParam("Accept") MediaType accept,
            @ApiParam(name = "e")
            @QueryParam("e") String e)
            throws NotFoundException {
        return Response.noContent().build();
    }
}
