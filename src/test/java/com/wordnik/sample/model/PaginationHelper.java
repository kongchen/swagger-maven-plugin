package com.wordnik.sample.model;

import io.swagger.annotations.ApiParam;

public class PaginationHelper {
    private Integer limit;
    private Integer offset;

    public PaginationHelper() {
    }

    public Integer getLimit() {
        return limit;
    }

    @ApiParam(value = "The pagination limit", name = "limit")
    public void setLimit(final Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    @ApiParam(value = "The pagination offset", name = "offset")
    public void setOffset(final Integer offset) {
        this.offset = offset;
    }
}
