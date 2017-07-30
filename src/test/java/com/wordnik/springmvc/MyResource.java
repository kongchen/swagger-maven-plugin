package com.wordnik.springmvc;

import com.wordnik.sample.model.ListItem;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Api(description = "Operations about pets")
@RequestMapping(value = "/myResourceImpl", produces = {"application/json", "application/xml"})
public interface MyResource {

    //contrived example test case for swagger-maven-plugin issue #505
    @RequestMapping(method = RequestMethod.GET, value = "list")
    @ApiOperation(value = "Get a list of items",
                  notes = "This is a contrived example"
    )
    public abstract List<ListItem> getListOfItems();
}
