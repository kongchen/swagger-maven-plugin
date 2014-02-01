package com.github.kongchen.swagger.docgen.mustache;

import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.AuthorizationScope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by kong on 14-2-1.
 */
public class MustacheAuthorization {
    private final List<AuthorizationScope> authorizationScopes = new ArrayList<AuthorizationScope>();
    private final String type;

    public MustacheAuthorization(Authorization authorization) {
        this.type = authorization.type();
        Collections.addAll(this.authorizationScopes, authorization.scopes());
    }

    public List<AuthorizationScope> getAuthorizationScopes() {
        return authorizationScopes;
    }

    public String getType() {
        return type;
    }
}
