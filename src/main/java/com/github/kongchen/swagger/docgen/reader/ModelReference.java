package com.github.kongchen.swagger.docgen.reader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.models.ExternalDocs;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;

import java.util.Map;

public class ModelReference implements Model {
    @JsonIgnore
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void setTitle(String s) {

    }


    @JsonIgnore
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(String s) {

    }


    @JsonIgnore
    @Override
    public Map<String, Property> getProperties() {
        return null;
    }

    @Override
    public void setProperties(Map<String, Property> map) {

    }


    @JsonIgnore
    @Override
    public Object getExample() {
        return null;
    }

    @Override
    public void setExample(Object o) {

    }


    @JsonIgnore
    @Override
    public ExternalDocs getExternalDocs() {
        return null;
    }

    private String reference;
    @JsonProperty("$ref")
    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(String s) {
        reference = s;
    }

    @Override
    public Object clone() {
        return null;
    }


    @JsonIgnore
    @Override
    public Map<String, Object> getVendorExtensions() {
        return null;
    }
}
