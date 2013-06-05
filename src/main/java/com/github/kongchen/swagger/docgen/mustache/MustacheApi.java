package com.github.kongchen.swagger.docgen.mustache;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.core.DocumentationEndPoint;

public class MustacheApi {
    int apiIndex;

    String path;

    String url;

    List<MustacheOperation> operations = new LinkedList<MustacheOperation>();

    @JsonIgnore
    private int opIndex = 1;

    public MustacheApi(String basePath, DocumentationEndPoint api) {
        this.path = api.getPath();
        if (this.path != null && !this.path.startsWith("/")) {
            this.path = "/" + this.path;
        }
        this.url = basePath + api.getPath();
    }

    public void addOperation(MustacheOperation operation) {
        operation.setOpIndex(this.opIndex++);
        operations.add(operation);
    }

    public int getApiIndex() {
        return apiIndex;
    }

    public void setApiIndex(int apiIndex) {
        this.apiIndex = apiIndex;
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

    public void setUrl(String url) {
        this.url = url;
    }

    public List<MustacheOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<MustacheOperation> operations) {
        this.operations = operations;
    }
}
