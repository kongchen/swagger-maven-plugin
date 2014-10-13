package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.LoginEndpoint;

/**
 * Created by chekong on 10/11/14.
 */
public class JLoginEndpoint implements CanBeSwaggerModel<LoginEndpoint> {
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public LoginEndpoint toSwaggerModel() {
        return new LoginEndpoint(url);
    }
}
