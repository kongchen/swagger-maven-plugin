package com.github.kongchen.swagger.docgen.remote.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.github.kongchen.swagger.docgen.remote.JModelPropertyDeserializer;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ModelProperty;
import com.wordnik.swagger.model.ModelRef;
import scala.Option;


/**
 * Created by chekong on 10/11/14.
 */
@JsonDeserialize(using = JModelPropertyDeserializer.class)
public class JModelProperty implements CanBeSwaggerModel<ModelProperty> {
    private String type;
    private String qualifiedType;
    private int position;
    private boolean required;
    private String description;
    private JAllowableValues allowableValues;
    private JModelRef items;

    public String get$ref() {
        return $ref;
    }

    public void set$ref(String $ref) {
        this.$ref = $ref;
    }

    private String $ref;

    public void setType(String type) {
        this.type = type;
    }

    public void setQualifiedType(String qualifiedType) {
        this.qualifiedType = qualifiedType;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAllowableValues(JAllowableValues allowableValues) {
        this.allowableValues = allowableValues;
    }

    public void setItems(JModelRef items) {
        this.items = items;
    }

    public String getType() {
        return type;
    }

    public String getQualifiedType() {
        return qualifiedType;
    }

    public int getPosition() {
        return position;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDescription() {
        return description;
    }

    public JAllowableValues getAllowableValues() {
        return allowableValues;
    }

    public JModelRef getItems() {
        return items;
    }

    @Override
    public ModelProperty toSwaggerModel() {
        if ($ref != null) {
            type = $ref;
        }
        return new ModelProperty(type, qualifiedType, position, required,
                Utils.getOption(description), allowableValues == null ? null : allowableValues.toSwaggerModel(),
                items == null ? Option.<ModelRef>empty() : Utils.getOption(items.toSwaggerModel()));
    }
}
