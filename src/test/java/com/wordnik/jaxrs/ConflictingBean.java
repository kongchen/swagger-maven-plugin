package com.wordnik.jaxrs;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.PathParam;

public class ConflictingBean {

    @ApiParam(name = "beanAttribute", type = "com.wordnik.sample.model.Pet", collectionFormat = "set")
    @PathParam("attribute")
    private String attribute;

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
