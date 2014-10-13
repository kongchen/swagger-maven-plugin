package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.ApiKey;

/**
 * Created by kongchen on 14/10/12.
 */
public class JApiKey implements CanBeSwaggerModel<ApiKey>{
    private String passAs;
    private String keyname;

    public String getPassAs() {
        return passAs;
    }

    public void setPassAs(String passAs) {
        this.passAs = passAs;
    }

    public String getKeyname() {
        return keyname;
    }

    public void setKeyname(String keyname) {
        this.keyname = keyname;
    }

    @Override
    public ApiKey toSwaggerModel() {
        return new ApiKey(keyname, passAs);
    }
}
