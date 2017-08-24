package com.wordnik.springmvc;

import com.wordnik.sample.model.Pet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
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

    /*
    @POST
    @Consumes({"application/json", "application/xml"})
    @ApiOperation(value = "Add a new pet to the store")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public Response addPet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true) Pet pet) {
        Pet updatedPet = petData.addPet(pet);
        return Response.ok().entity(updatedPet).build();
    }
    */

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
}
