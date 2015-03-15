package com.github.kongchen.springmvc.controller;

import com.github.kongchen.model.Car;
import com.github.kongchen.model.ForGeneric;
import com.github.kongchen.model.ForGeneric2;
import com.github.kongchen.model.G1;
import com.github.kongchen.model.G2;
import com.wordnik.swagger.annotations.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;



/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 5/13/13
 */
@Controller
@RequestMapping("/garage")
@Api(value = "/garage", description = "Operations about garages", position = 3)
public class GarageControllerV1 {
    @RequestMapping(value = "/{garageId}", method = RequestMethod.GET)
    @ApiOperation(value = "Find garages by Id", notes = "To get garage info /* <com.github.kongchen.model.G1> */",
            response = ForGeneric.class, position = 2,
            authorizations = @Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "anything", description = "nothing")}))
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Garage not found")})
    @ResponseBody
    public ForGeneric<G1> getGarageById(
            @ApiParam(value = "ID of garage that needs to be fetched", allowableValues = "range[1,100]",
                    required = true) @PathVariable("garageId") String garageId) {
        return new ForGeneric<G1>();
    }

    @RequestMapping(value = "/{garageId}", method = RequestMethod.POST)
    @ApiOperation(value = "Repair a broken car in garage", notes = "To repair car /*<com.github.kongchen.model.G2,com.github.kongchen.model.v2.Car>*/",
            response = ForGeneric2.class, position = 1,
            authorizations = @Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "anything", description = "nothing")}))
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Garage not found")})
    @ResponseBody
    public ForGeneric2<G2, com.github.kongchen.model.v2.Car> getGarageById(
            @ApiParam(value = "ID of garage", allowableValues = "range[1,100]",
                    required = true) @PathVariable("garageId") String garageId,
            @ApiParam(value = "broken car1", required = true) @RequestBody(required = true) Car car) {
        return new ForGeneric2<G2, com.github.kongchen.model.v2.Car>();
    }
}
