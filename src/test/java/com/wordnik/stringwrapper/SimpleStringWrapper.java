package com.wordnik.stringwrapper;

import com.fasterxml.jackson.annotation.JsonCreator;

public class SimpleStringWrapper {

    private String value;
    
    public SimpleStringWrapper() {
    }
    
    @JsonCreator
    public SimpleStringWrapper(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
