package com.github.kongchen.swagger.docgen.mustache;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.Parameter;

import static com.github.kongchen.swagger.docgen.TypeUtils.getTrueType;

public class MustacheParameter {
    private final String allowableValue;

    private final String access;

    private final String defaultValue;

    private String name;

    private final boolean required;

    private final String description;

    private final String type;

    private final String linkType;

    public MustacheParameter(Parameter para) {
        this.name = para.name();
        this.linkType = getTrueType(para.dataType());
        this.required = para.required();
        this.description = Utils.getStrInOption(para.description());
        this.type = para.dataType();
        this.defaultValue = Utils.getStrInOption(para.defaultValue());
        this.allowableValue = Utils.allowableValuesToString(para.allowableValues());
        this.access = Utils.getStrInOption(para.paramAccess());
    }

    String getDefaultValue() {
        return defaultValue;
    }

    public String getAllowableValue() {
        return allowableValue;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getAccess() {
        return access;
    }

    @Override
    public String toString() {
        ObjectMapper om = new ObjectMapper();
        try {
           return  om.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return null;
        }

    }

    public void setName(String name) {
        this.name = name;
    }
}
