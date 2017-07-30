package com.wordnik.springmvc;

import com.wordnik.sample.model.ListItem;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

public abstract class MyResourceAbstract implements MyResource {

    @Override
    public abstract List<ListItem> getListOfItems();

    @Override
    public abstract Response testParamInheritance(
            @PathParam("firstParamAbstract") String firstParam,
            @ApiParam(required = true) @QueryParam("secondParamAbstract") String secondParam,
            String thirdParam);
}
