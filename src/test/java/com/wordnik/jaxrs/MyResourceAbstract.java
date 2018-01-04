package com.wordnik.jaxrs;

import com.wordnik.sample.exception.NotFoundException;
import com.wordnik.sample.model.ListItem;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author daniele.orler
 */
public abstract class MyResourceAbstract<T> implements MyResource<T> {
    @Override
    public abstract Response getPetsById(Long startId, Long endId) throws NotFoundException;

    @Override
    public abstract List<ListItem> getListOfItems();

    @Override
    public abstract Response testParamInheritance(
            @PathParam("firstParamAbstract") String firstParam,
            @ApiParam(required = true) @QueryParam("secondParamAbstract") String secondParam,
            String thirdParam);
}
