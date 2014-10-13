package com.github.kongchen.swagger.docgen.remote.model;


import java.util.LinkedHashMap;
import java.util.List;

import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.ModelProperty;


/**
 * Created by chekong on 10/11/14.
 */
public class JModel implements CanBeSwaggerModel<Model> {
    private String id;
    private String name;
    private String qualifiedType;
    private LinkedHashMap<String, JModelProperty> properties;
    private String description;
    private String baseModel;
    private String discriminator;
    private List<String> subTypes;

    private List<String> required;

    public List<String> getRequired() {
        return required;
    }

    public void setRequired(List<String> required) {
        this.required = required;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQualifiedType(String qualifiedType) {
        this.qualifiedType = qualifiedType;
    }

    public void setProperties(LinkedHashMap<String, JModelProperty> properties) {
        this.properties = properties;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBaseModel(String baseModel) {
        this.baseModel = baseModel;
    }

    public void setDiscriminator(String discriminator) {
        this.discriminator = discriminator;
    }

    public void setSubTypes(List<String> subTypes) {
        this.subTypes = subTypes;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQualifiedType() {
        return qualifiedType;
    }

    public LinkedHashMap<String, JModelProperty> getProperties() {
        return properties;
    }

    public String getDescription() {
        return description;
    }

    public String getBaseModel() {
        return baseModel;
    }

    public String getDiscriminator() {
        return discriminator;
    }

    public List<String> getSubTypes() {
        return subTypes;
    }

    @Override
    public Model toSwaggerModel() {
        scala.collection.mutable.LinkedHashMap<String, ModelProperty> prop
                = new scala.collection.mutable.LinkedHashMap<String, ModelProperty>();
        for (String key : this.properties.keySet()) {
            JModelProperty v = this.properties.get(key);
            if (required != null && required.contains(key)) {
                v.setRequired(true);
            }
            prop.put(key, v.toSwaggerModel());
        }

        return new Model(id, name, qualifiedType, prop, Utils.getOption(description), Utils.getOption(baseModel),
                Utils.getOption(discriminator),
                Utils.toScalaImmutableList(subTypes));
    }
}
