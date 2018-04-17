package com.wordnik.jaxrs;

import com.wordnik.sample.model.Pet;
import com.wordnik.sample.model.PetName;
import org.springframework.web.bind.annotation.RequestParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/")
public class SwaggerlessResource {

    @GET
    @Path("/swaggerless/{petId : [0-9]}")
    public Pet getPetByName(@PathParam(value = "name") final String name) {
        // Just create and return a new pet
        final Pet pet = new Pet();
        pet.setName(new PetName(name));
        return pet;
    }

    public Pet notAnEndpoint(@PathParam(value = "name") final String name) {
        // Just create and return a new pet
        final Pet pet = new Pet();
        pet.setName(new PetName(name));
        return pet;
    }

    @Path("/swaggerless")
    public Pet notAnEndpointWithPath(@RequestParam(value = "name") final String name) {
        // Just create and return a new pet
        final Pet pet = new Pet();
        pet.setName(new PetName(name));
        return pet;
    }

    @Path("/swaggerless/subresource")
    public SwaggerlessSubresource subresourceEndpoint() {
        return new SwaggerlessSubresource();
    }
}
