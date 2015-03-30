package com.github.kongchen.springmvc.controller;


import com.github.kongchen.model.Customer;
import com.wordnik.swagger.annotations.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping(value = "/v2/car")
@Api(value = "/v2/car", description = "Operations about cars v2", position = 2,
        authorizations = @Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "car2", description = "nothing")}))
public class CarControllerV2 {
    @RequestMapping(value = "/{carId}", method = RequestMethod.GET)
    @ApiOperation(value = "Find car by ID", notes = "To get car info by car's Id",
            response = Customer.class, authorizations =  @Authorization(value = "oauth2", scopes = {@AuthorizationScope(scope = "car2", description = "car2 does get")}))
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Car not found")})
    @ResponseBody
    public Customer getCarById(
            @ApiParam(value = "ID of car that needs to be fetched", allowableValues = "range[10,20]",
                    required = true) @PathVariable("carId") String carId) {
        return new Customer();
    }
}


