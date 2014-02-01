package com.github.kongchen.swagger.docgen.mustache;

import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ApiDescription;

import java.util.LinkedList;
import java.util.List;

public class MustacheApi {
    private final String description;

    private String path;

    private final String url;

    private final List<MustacheOperation> operations = new LinkedList<MustacheOperation>();

    public MustacheApi(String basePath, ApiDescription api) {
        this.path = api.path();
        if (this.path != null && !this.path.startsWith("/")) {
            this.path = "/" + this.path;
        }
        this.url = basePath + api.path();
        this.description = Utils.getStrInOption(api.description());
    }

    public void addOperation(MustacheOperation operation) {
        operations.add(operation);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public List<MustacheOperation> getOperations() {
        return operations;
    }

    public String getDescription() {
        return description;
    }
}
