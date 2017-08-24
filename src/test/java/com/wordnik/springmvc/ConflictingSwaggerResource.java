package com.wordnik.springmvc;

import com.wordnik.sample.model.Pet;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(basePath = "/swaggerBasePath", produces = "application/xml", consumes = "application/xml")
@RequestMapping(value = "/springMvcBasePath", produces = "application/json", consumes = "application/json")
public class ConflictingSwaggerResource {

    @RequestMapping(value = "/{petId}", method = RequestMethod.GET)
    @ApiOperation(value = "", produces = "application/xml", consumes = "application/xml", httpMethod = "put",
                  response = String.class, responseContainer = "list", responseReference = "#/definitions/ListItem")
    @ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "list", reference = "#/definitions/ListItem")
    public Pet getPetById(
            @ApiParam(name = "id", type = "com.wordnik.sample.model.ListItem", collectionFormat = "list")
            @PathVariable("petId") Long petId) {
        return new Pet();
    }

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation("")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "list", reference = "#/definitions/ListItem")
    })
    public Pet addPet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true) Pet pet) {
        return pet;
    }

    @RequestMapping(value = "/stringList", method = RequestMethod.GET)
    @ApiOperation(value = "", response = String.class, responseContainer = "list")
    public ResponseEntity getStringList()
    {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/petList", method = RequestMethod.GET)
    @ApiOperation(value = "", response = String.class, responseContainer = "list")
    @ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "list", reference = "#/definitions/ListItem")
    public ResponseEntity<List<Pet>> getPetList()
    {
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/helloWorld", method = RequestMethod.GET)
    @ApiOperation("")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = Pet.class, responseContainer = "list", reference = "#/definitions/ListItem")
    })
    public ResponseEntity<String> helloWorld() {
        return new ResponseEntity(HttpStatus.OK);
    }
}
