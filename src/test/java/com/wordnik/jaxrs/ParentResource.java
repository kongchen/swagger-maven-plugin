/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wordnik.jaxrs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.ws.rs.Path;

/**
 *
 * @author pradeep.chaudhary
 */
@Path("/v1.0")
@Api
public class ParentResource {
    @Path("/sub")
    @ApiOperation(value="SubResource")
    public SubResource getStudyResource() {
        return new SubResource();
    }
}
