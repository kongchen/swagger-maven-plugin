package com.github.kongchen.swagger.docgen.mustache;

import com.wordnik.swagger.core.DocumentationParameter;

import static com.github.kongchen.swagger.docgen.TypeUtils.getTrueType;

public class MustacheParameter {
    String defaultValue;

    String name;

    boolean required;

    String description;

    String type;

    String linkType;

    public MustacheParameter(DocumentationParameter para) {
        this.name = para.getName();
        this.linkType = getTrueType(para.getDataType());
        this.required = para.required();
        this.description = para.getDescription();
        this.type = para.getDataType();
        this.defaultValue = para.defaultValue();
    }

    String getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }
}
