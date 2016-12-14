package com.wordnik.jaxrs;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * @author chekong on 15/5/9.
 */
public class MyBean extends MyParentBean {

    @ApiParam(value = "ID of pet that needs to be updated", required = true)
    @PathParam("petId")
    private String petId;

    @ApiParam(value = "Updated name of the pet", required = false, defaultValue = "defaultValue")
    @FormParam("name")
    private String name;

    @ApiParam(value = "Updated status of the pet", required = false, allowableValues = "value1, value2")
    @FormParam("status")
    private String status;

    @HeaderParam("myHeader")
    private String myHeader;

    @HeaderParam("intValue")
    private int intValue;

    @QueryParam(value = "listValue")
    private List<String> listValue;

    public String getMyheader() {
        return myHeader;
    }

    public void setmyHeader(String myHeader) {
        this.myHeader = myHeader;
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

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public List<String> getListValue() {
        return listValue;
    }

    public void setListValue(List<String> listValue) {
        this.listValue = listValue;
    }

    @HeaderParam("myHeaderOnMethod")
    @ApiParam(value = "Header annotated on method", required = false)
    public void setMyheadronmethod(String myheadronmethod) {
    }

    @HeaderParam("myLongHeaderOnMethod")
    @ApiParam(value = "Long header annotated on method", required = false)
    public void setMyintheaderonmethod(long mylongheaderonmethod) {
    }
}
