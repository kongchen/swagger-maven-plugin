/**
 * 
 */
package com.wordnik.jaxrs;

import javax.ws.rs.HeaderParam;

import io.swagger.annotations.ApiParam;

/**
 * @author Vinayak Hulawale [vinhulawale@gmail.com]
 *
 */
public class MyParentBean {
    
    @ApiParam(value = "Header from parent", required = false)
    @HeaderParam("myParentHeader")
    private String myParentheader;

    public String getMyParentheader() {
        return myParentheader;
    }

    public void setMyParentheader(String myParentheader) {
        this.myParentheader = myParentheader;
    }

}
