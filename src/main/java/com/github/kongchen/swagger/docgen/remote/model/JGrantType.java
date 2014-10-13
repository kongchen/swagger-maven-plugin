package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.GrantType;

/**
 * Created by chekong on 10/11/14.
 */
public class JGrantType implements CanBeSwaggerModel<GrantType> {

    private JImplicitGrant implicit;
    private JAuthorizationCodeGrant authorization_code;

    public JImplicitGrant getImplicit() {
        return implicit;
    }

    public void setImplicit(JImplicitGrant implicit) {
        this.implicit = implicit;
    }

    public JAuthorizationCodeGrant getAuthorization_code() {
        return authorization_code;
    }

    public void setAuthorization_code(JAuthorizationCodeGrant authorization_code) {
        this.authorization_code = authorization_code;
    }

    @Override
    public GrantType toSwaggerModel() {
        if (implicit != null) return implicit.toSwaggerModel();
        if (authorization_code != null) return authorization_code.toSwaggerModel();
        return null;
    }
}
