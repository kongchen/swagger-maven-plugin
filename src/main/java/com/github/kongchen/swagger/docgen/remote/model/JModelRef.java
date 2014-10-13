package com.github.kongchen.swagger.docgen.remote.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ModelRef;

/**
 * Created by chekong on 10/11/14.
 */
public class JModelRef implements CanBeSwaggerModel<ModelRef> {
    private String type;
    @JsonProperty("$ref")
    private String ref;
    private String qualifiedType;

    public void setType(String type) {
        this.type = type;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setQualifiedType(String qualifiedType) {
        this.qualifiedType = qualifiedType;
    }

    public String getType() {
        return type;
    }

    public String getRef() {
        return ref;
    }

    public String getQualifiedType() {
        return qualifiedType;
    }

    @Override
    public ModelRef toSwaggerModel() {
        return new ModelRef(type, Utils.getOption(ref), Utils.getOption(qualifiedType));
    }
}
