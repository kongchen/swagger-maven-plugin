package com.github.kongchen.swagger.docgen.remote.model;

import java.util.List;

import com.github.kongchen.swagger.docgen.remote.ListConverter;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.Operation;


/**
 * Created by chekong on 10/11/14.
 */
public class JApiDescription implements CanBeSwaggerModel<ApiDescription> {
    private String path;
    private String description;
    private List<JOperation> operations;
    private boolean hidden;

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public List<JOperation> getOperations() {
        return operations;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setOperations(List<JOperation> operations) {
        this.operations = operations;
    }

    @Override
    public ApiDescription toSwaggerModel() {
        return new ApiDescription(path, Utils.getOption(description),
                new ListConverter<JOperation, Operation>(operations).convert(), hidden);
    }
}
