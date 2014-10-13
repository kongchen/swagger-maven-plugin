package com.github.kongchen.swagger.docgen.remote.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.AuthorizationScope;

/**
 * Created by chekong on 10/11/14.
 */
public class JAuthorization implements CanBeSwaggerModel<Authorization> {
    @JsonProperty("oauth2")
    private JAuthorizationScope[] scopes;

    public void setScopes(JAuthorizationScope[] scopes) {
        this.scopes = scopes;
    }

    public JAuthorizationScope[] getScopes() {
        return scopes;
    }

    public Authorization toSwaggerModel() {
        if (scopes == null || scopes.length == 0) {
            return null;
        }
        AuthorizationScope[] ss = new AuthorizationScope[scopes.length];
        for (int i = 0; i < scopes.length; i++) {
            ss[i] = new AuthorizationScope(scopes[i].getScope(), scopes[i].getDescription());
        }

        return new Authorization("oauth2", ss);
    }
}
