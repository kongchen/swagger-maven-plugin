package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.TokenRequestEndpoint;

/**
 * Created by chekong on 10/11/14.
 */
public  class JTokenRequestEndpoint implements CanBeSwaggerModel<TokenRequestEndpoint> {
    private String url;
    private String clientIdName;
    private String clientSecretName;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setClientIdName(String clientIdName) {
        this.clientIdName = clientIdName;
    }

    public void setClientSecretName(String clientSecretName) {
        this.clientSecretName = clientSecretName;
    }

    public String getUrl() {
        return url;
    }

    public String getClientIdName() {
        return clientIdName;
    }

    public String getClientSecretName() {
        return clientSecretName;
    }

    public TokenRequestEndpoint toSwaggerModel() {
        return new TokenRequestEndpoint(url, clientIdName, clientSecretName);
    }
}
