package com.github.kongchen.jaxrs.model;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * Created by chekong on 8/6/14.
 */
@ApiModel
public class BadIdResponse {
    @ApiModelProperty
    private int badPosition;

    @ApiModelProperty
    private String description;

    public int getBadPosition() {
        return badPosition;
    }

    public void setBadPosition(int badPosition) {
        this.badPosition = badPosition;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
