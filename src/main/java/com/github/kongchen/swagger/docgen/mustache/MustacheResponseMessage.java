package com.github.kongchen.swagger.docgen.mustache;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ResponseMessage;

import static com.github.kongchen.swagger.docgen.TypeUtils.getTrueType;

public class MustacheResponseMessage {
    private final int code;

    private final String message;

    private final String type;

    private final String linkType;

    public MustacheResponseMessage(ResponseMessage responseMessage) {
        this.code = responseMessage.code();
        this.message = responseMessage.message();
        if (!responseMessage.responseModel().isEmpty()) {
            this.type = responseMessage.responseModel().get();
            this.linkType = getTrueType(responseMessage.responseModel().get());
        } else {
            this.type = null;
            this.linkType = null;
        }
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getLinkType() {
        return linkType;
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
