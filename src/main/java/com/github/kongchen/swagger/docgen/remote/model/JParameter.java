package com.github.kongchen.swagger.docgen.remote.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.kongchen.swagger.docgen.remote.JParameterDeserializer;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.AllowableValues;
import com.wordnik.swagger.model.Parameter;

/**
 * Created by chekong on 10/11/14.
 */
@JsonDeserialize(using = JParameterDeserializer.class)
public class JParameter implements CanBeSwaggerModel<Parameter> {
    private String name;
    private String description;
    private String defaultValue;
    private boolean required;
    private boolean allowMultiple;
    @JsonProperty("type")
    private String dataType;
    private JAllowableValues allowableValues;
    private String paramType;
    private String paramAccess;


    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setAllowableValues(JAllowableValues allowableValues) {
        this.allowableValues = allowableValues;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public void setParamAccess(String paramAccess) {
        this.paramAccess = paramAccess;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    public String getDataType() {
        return dataType;
    }

    public JAllowableValues getAllowableValues() {
        return allowableValues;
    }

    public String getParamType() {
        return paramType;
    }

    public String getParamAccess() {
        return paramAccess;
    }

    @Override
    public Parameter toSwaggerModel() {
        AllowableValues a = allowableValues == null ? null : allowableValues.toSwaggerModel();
        return new Parameter(name, Utils.getOption(description), Utils.getOption(defaultValue),required, allowMultiple,
                dataType, a, paramType, Utils.getOption(paramAccess));
    }

}
