package com.github.kongchen.swagger.docgen.mustache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ResponseMessage;

import java.util.List;
import java.util.ArrayList;

import static com.github.kongchen.swagger.docgen.TypeUtils.getTrueType;

public class MustacheEnumType implements Comparable<MustacheEnumType> {
    private final String name;

    private final List<String> allowableValues;

    public MustacheEnumType(String name, List<String> allowableValues) {
        this.name = name;
        this.allowableValues = new ArrayList<String>();
        for (String s : allowableValues) {
            this.allowableValues.add(s);
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getAllowableValues() {
        return allowableValues;
    }

    @Override
    public int compareTo(MustacheEnumType o) {
        if (o == null) {
            return 1;
        }
        return this.name.compareTo(o.getName());
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
}
