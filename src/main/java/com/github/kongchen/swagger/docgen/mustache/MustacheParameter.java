package com.github.kongchen.swagger.docgen.mustache;

import com.wordnik.swagger.core.DocumentationAllowableListValues;
import com.wordnik.swagger.core.DocumentationAllowableRangeValues;
import com.wordnik.swagger.core.DocumentationAllowableValues;
import com.wordnik.swagger.core.DocumentationParameter;

import java.util.List;

import static com.github.kongchen.swagger.docgen.TypeUtils.getTrueType;

public class MustacheParameter {
    private final String allowableValue;

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


        this.allowableValue = allowableValuesToString(para.allowableValues());
    }

    private String allowableValuesToString(DocumentationAllowableValues para) {
        if (para == null) {
            return null;
        }
        String values = "";
        if (para instanceof DocumentationAllowableListValues) {
            List<String> vlist = ((DocumentationAllowableListValues) para).getValues();
            for (String v : vlist) {
                values += v.trim() + ", ";
            }
            values = values.trim();
            values = values.substring(0, values.length() - 1);

        } else {
            Float max = ((DocumentationAllowableRangeValues) para).getMax();
            Float min = ((DocumentationAllowableRangeValues) para).getMin();
            values = min + " to " + max;
        }
        return values;
    }

    String getDefaultValue() {
        return defaultValue;
    }

    void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getAllowableValue() {
        return allowableValue;
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
