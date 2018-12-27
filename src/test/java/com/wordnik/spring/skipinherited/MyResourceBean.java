package com.wordnik.spring.skipinherited;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.wordnik.sample.model.ListItem;

@RestController
public class MyResourceBean implements MyResourceSI {

    @Override
    public List<ListItem> getListOfItems(String param) {
        return null;
    }
}
