package com.wordnik.sample.model;

import io.swagger.annotations.ApiModelProperty;

public class ConflictingApiModel {

    private long id;

    @ApiModelProperty(value = "Model Id", name = "modelId", dataType = "com.wordnik.sample.model.Pet")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
