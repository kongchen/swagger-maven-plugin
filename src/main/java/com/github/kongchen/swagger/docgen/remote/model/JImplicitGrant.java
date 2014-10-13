package com.github.kongchen.swagger.docgen.remote.model;

import com.wordnik.swagger.model.ImplicitGrant;

/**
 * Created by chekong on 10/11/14.
 */
public class JImplicitGrant implements CanBeSwaggerModel<ImplicitGrant> {
    private JLoginEndpoint loginEndpoint;
    private String tokenName;

    public JLoginEndpoint getLoginEndpoint() {
        return loginEndpoint;
    }

    public void setLoginEndpoint(JLoginEndpoint loginEndpoint) {
        this.loginEndpoint = loginEndpoint;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    @Override
    public ImplicitGrant toSwaggerModel() {
        return new ImplicitGrant(loginEndpoint.toSwaggerModel(), tokenName);
    }
}
