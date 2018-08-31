package com.wordnik.spring.skipinherited;

import com.wordnik.sample.model.ListItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(description = "Operations about pets")
@RequestMapping(value = "/myResourceSkipInherited", produces = {"application/json", "application/xml"})
public interface MyResourceSI {

    @RequestMapping(method = RequestMethod.GET, value = "list")
    @ApiOperation(value = "Get a list of items",
                  notes = "This is a contrived example"
    )
    public List<ListItem> getListOfItems(
            @RequestHeader(name = "X-Simple-Param", required = true) 
            @ApiParam(name = "X-Simple-Param",
            value = "The Simple Param", required = true,
            example = "ABC45678901234567") String param);
}
