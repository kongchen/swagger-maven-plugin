package com.wordnik.sample.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author chekong on 15/5/19.
 */
public class PetName {
    private final String name;

    @JsonCreator
    public static PetName fromString(@JsonProperty("name") String name) {

        return new PetName(name);
    }

    public PetName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
