package com.github.kongchen.swagger.docgen;

import java.util.List;

import com.wordnik.swagger.core.Documentation;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public abstract class AbstractDocumentSource {
    private String basePath;

    private String apiVersion;

    public abstract List<Documentation> toSwaggerDocuments() throws Exception;

    public String getBasePath() {
        return basePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
