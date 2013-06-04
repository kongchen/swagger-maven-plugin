package com.github.kongchen.swagger.docgen.mustache;

import com.github.kongchen.swagger.docgen.TypeUtils;
import com.wordnik.swagger.core.DocumentationSchema;

public class MustacheItem {
    String name;

    String type;

    String linkType;

    boolean required;

    String access;

    String description;

    String notes;

    public MustacheItem(String name, DocumentationSchema documentationSchema) {

        this.name = name;
        this.type = documentationSchema.getType();
        this.linkType = this.type;

        this.required = documentationSchema.required();
        this.access = documentationSchema.getAccess();
        this.description = documentationSchema.getDescription();
        this.notes = documentationSchema.getNotes();
        this.linkType = TypeUtils.filterBasicTypes(this.linkType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setTypeAsArray(String elementType) {
        this.type = "Array:" + elementType;
        setLinkType(TypeUtils.filterBasicTypes(elementType));
    }
}
