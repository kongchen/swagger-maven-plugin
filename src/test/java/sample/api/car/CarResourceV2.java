package sample.api.car;

import com.sun.jersey.api.NotFoundException;
import com.wordnik.swagger.annotations.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 5/13/13
 */
@Path("/v2/car.json")
@Api(value = "/v2/car", description = "Operations about cars")
@Produces( {"application/json"})
public class CarResourceV2 {
    @GET
    @Path("/{carId}")
    @ApiOperation(value = "Find car by ID", notes = "To get car info by car's Id",
            responseClass = "sample.model.Customer")
    @ApiErrors(value = {@ApiError(code = 400, reason = "Invalid ID supplied"),
            @ApiError(code = 404, reason = "Car not found")})
    public Response getCarById(
            @ApiParam(value = "ID of car that needs to be fetched", allowableValues = "range[10,20]",
                    required = true) @PathParam("carId") String carId)
            throws NotFoundException {
        return Response.noContent().build();
    }
}
