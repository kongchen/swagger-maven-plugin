package com.github.kongchen.mavenswagger.apigen.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
public class OutputTemplate {


    private String basePath;

    private String apiVersion;

    private List<MformatApiDocument> apiDocuments = new LinkedList<MformatApiDocument>();

    private Set<MformatDataType> dataTypes = new HashSet<MformatDataType>();

    public static String getJsonSchema() {
        ObjectMapper m = new ObjectMapper();
        try {
            JsonSchema js = m.generateJsonSchema(OutputTemplate.class);
            return m.writeValueAsString(js);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<MformatDataType> getDataTypes() {
        return dataTypes;
    }

    public void addDateType(MformatDataType dataType) {
        dataTypes.add(dataType);
    }

    public List<MformatApiDocument> getApiDocuments() {
        return apiDocuments;
    }

    public void setApiDocuments(List<MformatApiDocument> apiDocuments) {
        this.apiDocuments = apiDocuments;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }
}
