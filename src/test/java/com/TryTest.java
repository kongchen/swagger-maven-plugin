package com;

import com.wordnik.swagger.jaxrs.Reader;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.sample.resource.PetResource;
import org.testng.annotations.Test;

/**
 * Created by chekong on 14-11-12.
 */

public class TryTest {
    @Test
    public void testTry() {
        Reader reader = new Reader(new Swagger());
        Swagger swagger = reader.read(PetResource.class);
        System.out.println(swagger.getBasePath());
    }

}
