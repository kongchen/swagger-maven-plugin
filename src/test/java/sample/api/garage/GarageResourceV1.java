package sample.api.garage;

import com.sun.jersey.api.NotFoundException;
import com.wordnik.swagger.annotations.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 5/13/13
 */
@Path("/garage.json")
@Api(value = "/garage", description = "Operations about garages")
@Produces( {"application/json"})
public class GarageResourceV1 {
    @GET
    @Path("/{garageId}")
    @ApiOperation(value = "Find garages by Id", notes = "To get garage info",
            responseClass = "sample.model.Garage")
    @ApiErrors(value = {@ApiError(code = 400, reason = "Invalid ID supplied"),
            @ApiError(code = 404, reason = "Garage not found")})
    public Response getGarageById(
            @ApiParam(value = "ID of garage that needs to be fetched", allowableValues = "range[1,100]",
                    required = true) @PathParam("garageId") String garageId)
            throws NotFoundException {
        return Response.noContent().build();
    }

    @POST
    @Path("/{garageId}")
    @ApiOperation(value = "Repair a broken car in garage", notes = "To repair car",
            responseClass = "sample.model.v2.Car")
    @ApiErrors(value = {@ApiError(code = 400, reason = "Invalid ID supplied"),
            @ApiError(code = 404, reason = "Garage not found")})
    public Response getGarageById(
            @ApiParam(value = "ID of garage", allowableValues = "range[1,100]",
                    required = true) @PathParam("garageId") String garageId,
            @ApiParam(value = "broken car1", required = true) sample.model.Car car)
            throws NotFoundException {
        return Response.noContent().build();
    }
}
