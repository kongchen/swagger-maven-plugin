package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.AuthorizationCodeGrant;

/**
 * Created by chekong on 10/11/14.
 */
public class JAuthorizationCodeGrant implements CanBeSwaggerModel<AuthorizationCodeGrant> {
    private JTokenRequestEndpoint tokenRequestEndpoint;
    private JTokenEndpoint tokenEndpoint;

    public JTokenRequestEndpoint getTokenRequestEndpoint() {
        return tokenRequestEndpoint;
    }

    public void setTokenRequestEndpoint(JTokenRequestEndpoint tokenRequestEndpoint) {
        this.tokenRequestEndpoint = tokenRequestEndpoint;
    }

    public JTokenEndpoint getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(JTokenEndpoint tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    @Override
    public AuthorizationCodeGrant toSwaggerModel() {
        return new AuthorizationCodeGrant(tokenRequestEndpoint.toSwaggerModel(), tokenEndpoint.toSwaggerModel());
    }
}
