package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.AuthorizationScope;

/**
 * Created by chekong on 10/11/14.
 */
public class JAuthorizationScope implements CanBeSwaggerModel<AuthorizationScope> {
    private String scope;
    private String description;

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getScope() {
        return scope;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public AuthorizationScope toSwaggerModel() {
        return new AuthorizationScope(scope, description);
    }
}
