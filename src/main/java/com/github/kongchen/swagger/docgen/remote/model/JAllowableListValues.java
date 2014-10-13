package com.github.kongchen.swagger.docgen.remote.model;

import java.util.List;

import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.AllowableListValues;


/**
 * Created by chekong on 10/11/14.
 */
public class JAllowableListValues  extends JAllowableValues{
    private List<String> values;
    private String valueType;

    public void setValues(List<String> values) {
        this.values = values;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public List<String> getValues() {
        return values;
    }

    public String getValueType() {
        return valueType;
    }

    @Override
    public AllowableListValues toSwaggerModel() {
        return new AllowableListValues(Utils.toScalaImmutableList(values), valueType);
    }
}
