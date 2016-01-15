package com.github.kongchen.swagger.docgen.util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api
abstract class TestApiA {
    @ApiOperation("Get count")
    public Integer getCount() {
        return null;
    }

    @ApiOperation("Get name")
    public abstract String getName();
}