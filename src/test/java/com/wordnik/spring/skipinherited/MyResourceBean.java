package com.wordnik.spring.skipinherited;

import java.util.List;

import com.wordnik.sample.model.ListItem;

public class MyResourceBean implements MyResourceSI {

    @Override
    public List<ListItem> getListOfItems(String param) {
        return null;
    }
}
