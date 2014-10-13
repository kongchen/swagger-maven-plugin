package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.TokenEndpoint;

/**
 * Created by chekong on 10/11/14.
 */
public class JTokenEndpoint implements CanBeSwaggerModel<TokenEndpoint> {
    private String url;
    private String tokenName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public TokenEndpoint toSwaggerModel() {
        return new TokenEndpoint(url, tokenName);
    }
}
