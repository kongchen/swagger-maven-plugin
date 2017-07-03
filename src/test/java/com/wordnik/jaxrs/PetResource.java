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

package com.wordnik.jaxrs;

import com.sun.jersey.api.core.InjectParam;
import com.wordnik.sample.JavaRestResourceUtil;
import com.wordnik.sample.TestVendorExtension;
import com.wordnik.sample.data.PetData;
import com.wordnik.sample.model.Pet;
import com.wordnik.sample.model.PetName;
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

import java.util.List;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/pet")
@Api(value = "/pet", description = "Operations about pets", authorizations = {
        @Authorization(value = "petstore_auth",
                scopes = {
                        @AuthorizationScope(scope = "write:pets", description = "modify pets in your account"),
                        @AuthorizationScope(scope = "read:pets", description = "read your pets")
                })
})
@Produces({"application/json", "application/xml"})
public class PetResource {
    static PetData petData = new PetData();
    static JavaRestResourceUtil ru = new JavaRestResourceUtil();

    @GET
    @Path("/{petId : [0-9]}")
    @ApiOperation(value = "Find pet by ID",
            notes = "Returns a pet when ID < 10.  ID > 10 or nonintegers will simulate API error conditions",
            response = Pet.class,
            authorizations = @Authorization(value = "api_key")
    )
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found")})
    public Response getPetById(
            @ApiParam(value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]", required = true) @PathParam("petId") Long petId)
            throws com.wordnik.sample.exception.NotFoundException {
        Pet pet = petData.getPetbyId(petId);
        if (pet != null) {
            return Response.ok().entity(pet).build();
        } else {
            throw new com.wordnik.sample.exception.NotFoundException(404, "Pet not found");
        }
    }
    //contrived example test case for swagger-maven-plugin issue #304
    @GET
    @Path("/{startId : [0-9]{1,2}}:{endId : [0-9]{1,2}}")
    @ApiOperation(value = "Find pet(s) by ID",
            notes = "This is a contrived example of a path segment containing multiple path parameters, separated by a character which may be present in the path parameter template. You may think that it returns a range of pets from startId to endId, inclusive, but it doesn't.",
            response = Pet.class,
            authorizations = @Authorization(value = "api_key")
    )
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found")})
    public Response getPetsById(
            @ApiParam(value = "start ID of pets that need to be fetched", allowableValues = "range[1,99]", required = true) @PathParam("startId") Long startId,
            @ApiParam(value = "end ID of pets that need to be fetched", allowableValues = "range[1,99]", required = true) @PathParam("endId") Long endId)
            throws com.wordnik.sample.exception.NotFoundException {
        Pet pet = petData.getPetbyId(startId);
        if (pet != null) {
            return Response.ok().entity(pet).build();
        } else {
            throw new com.wordnik.sample.exception.NotFoundException(404, "Pet not found");
        }
    }

    @DELETE
    @Path("/{petId}")
    @ApiOperation(value = "Deletes a pet", nickname = "removePet")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid pet value")})
    public Response deletePet(
            @ApiParam() @HeaderParam("api_key") String apiKey,
            @ApiParam(value = "Pet id to delete", required = true) @PathParam("petId") Long petId) {
        petData.deletePet(petId);
        return Response.ok().build();
    }

    @POST
    @Consumes({"application/json", "application/xml"})
    @ApiOperation(value = "Add a new pet to the store")
    @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
    public Response addPet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true) Pet pet) {
        Pet updatedPet = petData.addPet(pet);
        return Response.ok().entity(updatedPet).build();
    }

    @PUT
    @Consumes({"application/json", "application/xml"})
    @ApiOperation(value = "Update an existing pet")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
            @ApiResponse(code = 404, message = "Pet not found"),
            @ApiResponse(code = 405, message = "Validation exception")})
    public Response updatePet(
            @ApiParam(value = "Pet object that needs to be added to the store", required = true) Pet pet) {
        Pet updatedPet = petData.addPet(pet);
        return Response.ok().entity(updatedPet).build();
    }

    @GET
    @Path("/pets/{petName : [^/]*}")
    @ApiOperation(value = "Finds Pets by name",
            response = Pet.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid status value")})
    public Response findPetByPetName(
            @ApiParam(value = "petName", required = true)
            @PathParam("petName") PetName petName) {
        return Response.ok(petData.getPetbyId(1)).build();
    }

    @GET
    @Path("/findByStatus")
    @ApiOperation(value = "Finds Pets by status",
            notes = "Multiple status values can be provided with comma seperated strings",
            response = Pet.class,
            responseContainer = "List")
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid status value")})
    public Response findPetsByStatus(
            @ApiParam(
                    value = "Status values that need to be considered for filter",
                    required = true,
                    defaultValue = "available",
                    allowableValues = "available,pending,sold",
                    allowMultiple = true)
            @QueryParam("status") String status) {
        return Response.ok(petData.findPetByStatus(status)).build();
    }

    @GET
    @Path("/findByTags")
    @ApiOperation(value = "Finds Pets by tags",
            notes = "Muliple tags can be provided with comma seperated strings. Use tag1, tag2, tag3 for testing.",
            response = Pet.class,
            responseContainer = "List")
    @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid tag value")})
    @Deprecated
    public Response findPetsByTags(
            @ApiParam(value = "Tags to filter by", required = true, allowMultiple = true) @QueryParam("tags") String tags) {
        return Response.ok(petData.findPetByTags(tags)).build();
    }

	@GET
	@Path("/findAll")
	@ApiOperation(value = "Finds all Pets", notes = "Returns a paginated list of all the Pets.")
	@ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid page number value") })
	public PagedList<Pet> findAllPaginated(
			@ApiParam(value = "pageNumber", required = true) @QueryParam("pageNumber") int pageNumber) {
		List<Pet> allPets = petData.findAllPets();
		int pageSize = 5;
		int startIndex = (pageNumber - 1) * pageSize;
		int endIndex = startIndex + pageSize;
		return new PagedList<Pet>(pageNumber, allPets.size(), allPets.subList(startIndex, endIndex));
	}

    @POST
    @Path("/{petId}")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @ApiOperation(value = "Updates a pet in the store with form data",
            consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @ApiResponses(value = {
            @ApiResponse(code = 405, message = "Invalid input")})
    public Response updatePetWithForm(
            @BeanParam MyBean myBean) {
        System.out.println(myBean.getName());
        System.out.println(myBean.getStatus());
        return Response.ok().entity(new com.wordnik.sample.model.ApiResponse(200, "SUCCESS")).build();
    }

    @POST
    @Path("/{petId}/testInjectParam")
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED})
    @ApiOperation(value = "Updates a pet in the store with form data",
            consumes = MediaType.APPLICATION_FORM_URLENCODED)
    @ApiResponses(value = {
            @ApiResponse(code = 405, message = "Invalid input")})
    public Response updatePetWithFormViaInjectParam(
            @InjectParam MyBean myBean) {
        System.out.println(myBean.getName());
        System.out.println(myBean.getStatus());
        return Response.ok().entity(new com.wordnik.sample.model.ApiResponse(200, "SUCCESS")).build();
    }

    @ApiOperation(value = "Returns pet", response = Pet.class)
    @GET
    @Produces("application/json")
    public Pet get(@ApiParam(hidden = true, name = "hiddenParameter") @QueryParam("hiddenParameter") String hiddenParameter) {
        return new Pet();
    }

    @ApiOperation(value = "Test pet as json string in query", response = Pet.class)
    @GET
    @Path("/test")
    @Produces("application/json")
    public Pet test(
            @ApiParam(value = "describe Pet in json here")
            @QueryParam("pet") Pet pet) {
        return new Pet();
    }

    @GET
    @Path("/test/extensions")
    @Produces("text/plain")
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

    @ApiOperation(value = "Test apiimplicitparams", response = Pet.class)
    @GET
    @Path("/test/apiimplicitparams/{path-test-name}")
    @Produces("application/json")
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
    public Pet testapiimplicitparams() {
        return new Pet();
    }

    @ApiOperation(value = "Test testFormApiImplicitParams", response = Pet.class)
    @GET
    @Path("/test/testFormApiImplicitParams")
    @Produces("application/json")
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
    public Pet testFormApiImplicitParams() {
        return new Pet();
    }

    @ApiOperation(value = "testingHiddenApiOperation", hidden = true)
    @GET
    @Produces("application/json")
    public String testingHiddenApiOperation() {
        return "testingHiddenApiOperation";
    }

    @ApiOperation(value = "testingBasicAuth", authorizations = @Authorization(value = "basicAuth"))
    @GET
    @Path("/test/testingBasicAuth")
    public String testingBasicAuth() {
        return "testingBasicAuth";
    }

    @ApiOperation(value = "testingArrayResponse")
    @ApiResponses(@ApiResponse(code = 200, message = "array", response = Pet.class, responseContainer = "List"))
    @GET
    @Path("/test/testingArrayResponse")
    public Response testingArrayResponse() {
        return null;
    }

    @ApiOperation("testingVendorExtensions")
    @TestVendorExtension.TestVendorAnnotation
    @GET
    @Path("/test/testingVendorExtensions")
    public Response testingVendorExtensions() {
        return null;
    }
}
