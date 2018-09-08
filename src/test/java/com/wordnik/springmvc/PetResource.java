/**
 * Copyright 2014 Reverb Technologies, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wordnik.springmvc;

import com.wordnik.sample.JavaRestResourceUtil;
import com.wordnik.sample.TestVendorExtension;
import com.wordnik.sample.data.PetData;
import com.wordnik.sample.exception.NotFoundException;
import com.wordnik.sample.model.PaginationHelper;
import com.wordnik.sample.model.Pet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Api(value = "/pet", description = "Operations about pets", authorizations = {
        @Authorization(value = "petstore_auth",
                scopes = {
                        @AuthorizationScope(scope = "write:pets", description = "modify pets in your account"),
                        @AuthorizationScope(scope = "read:pets", description = "read your pets")
                })
})

@RequestMapping(value = "/pet", produces = {"application/json", "application/xml"})
public class PetResource {

    static PetData petData = new PetData();
    static JavaRestResourceUtil ru = new JavaRestResourceUtil();

    @RequestMapping(value = "/{petId}", method = RequestMethod.GET)
    @ApiOperation(value = "Find pet by ID",
            notes = "Returns a pet when ID < 10.  ID > 10 or nonintegers will simulate API error conditions",
            authorizations = @Authorization(value = "api_key")
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Pet data found", response = Pet.class),
            @ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found")})
    public ResponseEntity<Pet> getPetById(
            @ApiParam(value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]", required = true)
            @PathVariable("petId") Long petId)
            throws NotFoundException {
        Pet pet = petData.getPetbyId(petId);
        if (pet != null) {
            return new ResponseEntity(pet, HttpStatus.OK);
        } else {
            throw new NotFoundException(404, "Pet not found");
        }
    }

    @RequestMapping(value = "/{petId}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Deletes a pet", nickname = "removePet")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid pet value")})
    public ResponseEntity deletePet(
            @RequestHeader("api_key") String apiKey,
            @ApiParam(value = "Pet id to delete", required = true)
            @PathVariable("petId")
            @Size(min = 0, max = Integer.MAX_VALUE) Long petId) {
        petData.deletePet(petId);
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(consumes = {"application/json", "application/xml"}, method = RequestMethod.POST)
    @ApiOperation(value = "Add a new pet to the store")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public ResponseEntity<Pet> addPet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true)
            @RequestBody Pet pet) {
        Pet updatedPet = petData.addPet(pet);
        return new ResponseEntity<Pet>(updatedPet, HttpStatus.OK);
    }

    @RequestMapping(value = "/pets", consumes = {"application/json", "application/xml"}, method = RequestMethod.POST)
    @ApiOperation(value = "Add multiple pets to the store")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public ResponseEntity<List<Pet>> addMultiplePets(
            @ApiParam(value = "A list of pet objects that need to be added to the store", required = true)
            @RequestBody Set<Pet> pets) {
        List<Pet> createdPets = new ArrayList<Pet>();
        for (Pet pet : pets) {
            createdPets.add(petData.addPet(pet));
        }
        return new ResponseEntity<List<Pet>>(createdPets, HttpStatus.OK);
    }

    @RequestMapping(consumes = {"application/json", "application/xml"}, method = RequestMethod.PUT)
    @ApiOperation(value = "Update an existing pet")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found"),
            @ApiResponse(code = 405, message = "Validation exception")})
    public ResponseEntity<Pet> updatePet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true)
            @RequestBody Pet pet) {
        Pet updatedPet = petData.addPet(pet);
        return new ResponseEntity<Pet>(updatedPet, HttpStatus.OK);
    }

    @RequestMapping(value = "/findByStatus", method = RequestMethod.GET)
    @ApiOperation(value = "Finds Pets by status",
            notes = "Multiple status values can be provided with comma seperated strings",
            response = Pet.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid status value")})
    public List<Pet> findPetsByStatus(
            @ApiParam(value = "Status values that need to be considered for filter", required = true, defaultValue = "available", allowableValues = "available,pending,sold", allowMultiple = true)
            @RequestParam("status") String status) {
        return petData.findPetByStatus(status);
    }

    @RequestMapping(value = "/findByStatuses", method = RequestMethod.GET)
    @ApiOperation(value = "Finds Pets by status",
            notes = "Multiple status values can be provided with multiple query parameters. Example: ?status=sold&status=pending",
            response = Pet.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Operation successful, and items were found matching the query. Data in response body."),
            @ApiResponse(code = 400, message = "Invalid status value")
    })
    public List<Pet> findPetsByStatuses(
            @ApiParam(value = "Status values that need to be considered for filter", required = true, defaultValue = "available", allowableValues = "available,pending,sold")
            @RequestParam("status") List<String> statuses) {
        return petData.findPetByStatus(StringUtils.join(statuses, ","));
    }

    @RequestMapping(value = "/findByTags", method = RequestMethod.GET)
    @ApiOperation(value = "Finds Pets by tags",
            notes = "Muliple tags can be provided with comma seperated strings. Use tags=tag1,tag2,tag3 for testing.",
            response = Pet.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid tag value")})
    @Deprecated
    public List<Pet> findPetsByTags(
            @ApiParam(value = "Tags to filter by", required = true, allowMultiple = true)
            @RequestParam("tags") String tags) {
        return petData.findPetByTags(tags);
    }

    @RequestMapping(value = "/pets", method = RequestMethod.GET)
    @ApiOperation(value = "Retrieve all pets. Pagination supported",
            notes = "If you wish to paginate the results of this API, supply offset and limit query parameters.",
            response = Pet.class,
            responseContainer = "List")
    public Map<String, Object> getAllPetsPaginated(@ModelAttribute PaginationHelper paginationHelper) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("limit", paginationHelper.getLimit());
        map.put("offset", paginationHelper.getOffset());
        map.put("results", petData.findPetByStatus("available,sold,pending")); // TODO: implement paginated getter for petData
        return map;
    }

    @RequestMapping(value = "/{petId}", consumes = {"application/x-www-form-urlencoded"}, method = RequestMethod.POST)
    @ApiOperation(value = "Updates a pet in the store with form data", consumes = "application/x-www-form-urlencoded")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public ResponseEntity<com.wordnik.sample.model.ApiResponse> updatePetWithForm(@ModelAttribute UpdatePetRequest updatePetRequest) {
        System.out.println(updatePetRequest.getName());
        System.out.println(updatePetRequest.getStatus());
        return new ResponseEntity<com.wordnik.sample.model.ApiResponse>(new com.wordnik.sample.model.ApiResponse(200, "SUCCESS"), HttpStatus.OK);
    }

    @ApiOperation(value = "Returns pet", response = Pet.class)
    @RequestMapping(produces = "application/json", method = RequestMethod.GET)
    public Pet get() {
        return new Pet();
    }

    @ApiOperation(value = "ping the service")
    @RequestMapping(value = "/petPing", method = RequestMethod.GET)
    public ResponseEntity<String> petPing(
            @ApiParam(hidden = true, name = "thisShouldBeHidden")
            @RequestParam(required = false) Map<String, String> hiddenParameter) {
        return new ResponseEntity<String>("Pet pong", HttpStatus.OK);
    }

    @RequestMapping(value = "/test/extensions", method = RequestMethod.GET)
    @ApiOperation(value = "testExtensions",
            extensions = {
                    @Extension(name = "firstExtension", properties = {
                            @ExtensionProperty(name = "extensionName1", value = "extensionValue1"),
                            @ExtensionProperty(name = "extensionName2", value = "extensionValue2")}),
                    @Extension(properties = {
                            @ExtensionProperty(name = "extensionName3", value = "extensionValue3")})
            }
    )
    public Pet testingExtensions() {
        return new Pet();
    }

    @ApiOperation(value = "testingHiddenApiOperation", hidden = true)
    @RequestMapping(value = "/testingHiddenApiOperation", method = RequestMethod.GET)
    public ResponseEntity<String> testingHiddenApiOperation() {
        return new ResponseEntity<String>("testingHiddenApiOperation", HttpStatus.OK);
    }

    @ApiOperation(value = "testing")
    @RequestMapping(value = "/testing", method = RequestMethod.GET)
    public Object testing(@ApiParam(name = "items") @RequestParam(value = "items") String[] items) {
        return new Object();
    }

    @ApiOperation(value = "testingApiImplicitParams")
    @RequestMapping(
            value = "/testingApiImplicitParams/{path-test-name}",
            method = RequestMethod.GET,
            produces = "application/json")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(
                    name = "header-test-name",
                    value = "header-test-value",
                    required = true,
                    dataType = "string",
                    paramType = "header",
                    defaultValue = "z"),

            @ApiImplicitParam(
                    name = "path-test-name",
                    value = "path-test-value",
                    required = true,
                    dataType = "string",
                    paramType = "path",
                    defaultValue = "path-test-defaultValue"),

            @ApiImplicitParam(
                    name = "body-test-name",
                    value = "body-test-value",
                    required = true,
                    dataType = "com.wordnik.sample.model.Pet",
                    paramType = "body")
    })
    public String testingApiImplicitParams() {
        return "testing";
    }

    @ApiOperation(value = "testingFormApiImplicitParam")
    @RequestMapping(
            value = "/testingFormApiImplicitParam",
            method = RequestMethod.GET,
            produces = "application/json")
    @ApiImplicitParams(value = {
            @ApiImplicitParam(
                    name = "form-test-name",
                    value = "form-test-value",
                    allowMultiple = true,
                    required = true,
                    dataType = "string",
                    paramType = "form",
                    defaultValue = "form-test-defaultValue")

    })
    public String testingFormApiImplicitParam() {
        return "testing";
    }

    @ApiOperation(value = "testingBasicAuth", authorizations = @Authorization(value = "basicAuth"))
    @RequestMapping(
            value = "/testingBasicAuth",
            method = RequestMethod.GET,
            produces = "application/json")
    public String testingBasicAuth() {
        return "testingBasicAuth";
    }

    @ApiOperation("testingVendorExtensions")
    @TestVendorExtension.TestVendorAnnotation
    @RequestMapping(
            value = "/testingVendorExtensions",
            method = RequestMethod.GET,
            produces = "application/json")
    public String testingVendorExtensions() {
        return null;
    }

    @ApiOperation("testingMergedAnnotations")
    @GetMapping(
            value = "/testingMergedAnnotations",
            produces = "application/json")
    public String testingMergedAnnotations() {
        return "it works";
    }
}
