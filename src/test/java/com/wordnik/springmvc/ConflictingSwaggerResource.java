package com.wordnik.springmvc;

import com.wordnik.sample.model.Pet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Api
@RequestMapping(value = "/springMvcBasePath", produces = "application/json", consumes = "application/json")
public class ConflictingSwaggerResource {

    @RequestMapping(value = "/{petId}", method = RequestMethod.GET)
    @ApiOperation("")
    public Pet getPetById(@PathVariable("petId") Long petId) {
        return new Pet();
    }
}
