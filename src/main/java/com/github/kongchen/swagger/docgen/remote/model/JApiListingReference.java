package com.github.kongchen.swagger.docgen.remote.model;

import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ApiListingReference;

/**
 * Created by chekong on 10/11/14.
 */
public class JApiListingReference implements CanBeSwaggerModel<ApiListingReference> {

    private String path;
    private String description;
    private int position;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public ApiListingReference toSwaggerModel() {
        return new ApiListingReference(path, Utils.getOption(description), position);
    }
}
