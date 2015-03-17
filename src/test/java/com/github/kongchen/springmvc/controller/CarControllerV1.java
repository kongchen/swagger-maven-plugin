package com.github.kongchen.springmvc.controller;


import com.github.kongchen.model.BadIdResponse;
import com.github.kongchen.model.Car;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import com.wordnik.swagger.annotations.AuthorizationScope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/car")
@Api(value = "/car", description = "Operations about cars v1", position = 1, protocols = "http")
public class CarControllerV1 {
    @RequestMapping(value = "/{carId}", method = RequestMethod.GET)
    @ApiOperation(value = "Find car by ID", notes = "To get car info by car's Id",
            response = Car.class, position = 2,
            authorizations = @Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "car1", description = "car1 des get")}))
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied", response = BadIdResponse.class),
            @ApiResponse(code = 404, message = "Car not found")})
    @ApiImplicitParams(value = {@ApiImplicitParam(name = "ETag", paramType = "response_header", value = "version", dataType = "string")})
    @ResponseBody
    public Car getCarById(
            @ApiParam(value = "ID of car that needs to be fetched", allowableValues = "range[1,10]",
                    required = true) @PathVariable("carId") String carId,
            @ApiParam(allowableValues = "application/json, application/*")
            @RequestHeader(value="Accept", required = false) MediaType accept,
            @ApiParam(name = "e")
            @RequestParam("e") String e) {
        return new Car();
    }

    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "search cars", notes = "Search cars by query",
            response = Car.class, responseContainer = "List", position = 1,
            authorizations =
            @Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "car1", description = "car1 des")}))
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Bad query")})
    @ResponseBody
    public List<Car> getCars(
            @ApiParam(allowableValues = "application/json, application/*")
            @RequestHeader(value="Accept", required = false) MediaType accept,
            @ApiParam(name = "query")
            @RequestParam("query") String query) {
        return new ArrayList<Car>();
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ApiOperation(value = "remove a car", position = 4)
    @ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "version not match")
    public void deleteCar(
            @ApiParam(name = "version")
            @RequestHeader(value = "version")
            String version,
            @ApiParam("carId")
            @RequestParam("id")
            String carid
    ) {
        return;
    }
}
