package com.wordnik.jaxrs;

import com.wordnik.sample.model.Pet;
import com.wordnik.sample.model.PetName;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class SwaggerlessSubresource {

    @GET
    @Path("/{name}")
    public Pet getPetByNameSubresource(@PathParam(value = "name") String name) {
        // Just create and return a new pet
        Pet pet = new Pet();
        pet.setName(new PetName(name));
        return pet;
    }
}
