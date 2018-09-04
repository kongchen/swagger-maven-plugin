package com.wordnik.springmvc;

import com.wordnik.sample.model.Pet;
import com.wordnik.sample.model.PetName;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerlessResource {

    @RequestMapping(value = "/swaggerless/{petId}", method = RequestMethod.GET)
    public Pet getPetByName(@PathVariable(value = "name") String name) {
        // Just create and return a new pet
        Pet pet = new Pet();
        pet.setName(new PetName(name));
        return pet;
    }

    public Pet notAnEndpoint(@PathVariable(value = "name") String name) {
        // Just create and return a new pet
        Pet pet = new Pet();
        pet.setName(new PetName(name));
        return pet;
    }
}
