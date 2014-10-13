package com.github.kongchen.swagger.docgen.remote.model;

import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ResponseMessage;

/**
 * Created by chekong on 10/11/14.
 */
public  class JResponseMessage implements CanBeSwaggerModel<ResponseMessage> {
    private int code;
    private String message;
    private String responseModel;


    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setResponseModel(String responseModel) {
        this.responseModel = responseModel;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getResponseModel() {
        return responseModel;
    }

    @Override
    public ResponseMessage toSwaggerModel() {
        return new ResponseMessage(code, message, Utils.getOption(responseModel));
    }
}
