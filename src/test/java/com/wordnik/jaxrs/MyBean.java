package com.wordnik.jaxrs;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;

/**
 * Created by chekong on 15/5/9.
 */
public class MyBean {
    @ApiParam(value = "ID of pet that needs to be updated", required = true)
    @PathParam("petId")
    private String petId;

    @ApiParam(value = "Updated name of the pet", required = false)
    @FormParam("name")
    private String name;

    @ApiParam(value = "Updated status of the pet", required = false)
    @FormParam("status")
    private String status;

    @HeaderParam("myHeader")
    private String myheadr;

    public String getMyheadr() {
        return myheadr;
    }

    public void setMyheadr(String myheadr) {
        this.myheadr = myheadr;
    }

    public String getPetId() {
        return petId;
    }

    public void setPetId(String petId) {
        this.petId = petId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
