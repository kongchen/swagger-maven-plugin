package com.wordnik.springmvc;

import com.wordnik.sample.model.Pet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api(basePath = "/swaggerBasePath", produces = "application/xml", consumes = "application/xml")
@RequestMapping(value = "/springMvcBasePath", produces = "application/json", consumes = "application/json")
public class ConflictingSwaggerResource {

    @RequestMapping(value = "/{petId}", method = RequestMethod.GET)
    @ApiOperation(value = "", produces = "application/xml", consumes = "application/xml", httpMethod = "put",
                  response = String.class, responseContainer = "list", responseReference = "#/definitions/ListItem")
    public Pet getPetById(
            @ApiParam(name = "id", type = "com.wordnik.sample.model.ListItem", collectionFormat = "list")
            @PathVariable("petId") Long petId) {
        return new Pet();
    }
}
