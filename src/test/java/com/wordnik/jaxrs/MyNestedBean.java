package com.wordnik.jaxrs;

import javax.ws.rs.HeaderParam;

import io.swagger.annotations.ApiParam;

/**
 * Represents a {@code @BeanParam} target that is nested within another bean.
 */
public class MyNestedBean {
    
    @ApiParam(value = "Header from nested bean", required = false)
    @HeaderParam("myNestedBeanHeader")
    private String myNestedBeanHeader;

    public String getMyNestedBeanHeader() {
        return myNestedBeanHeader;
    }
    
    public void setMyNestedBeanHeader(String myNestedBeanHeader) {
        this.myNestedBeanHeader = myNestedBeanHeader;
    }
}
