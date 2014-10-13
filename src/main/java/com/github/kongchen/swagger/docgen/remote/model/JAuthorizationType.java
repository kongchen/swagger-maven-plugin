package com.github.kongchen.swagger.docgen.remote.model;

import com.github.kongchen.swagger.docgen.remote.ListConverter;
import com.wordnik.swagger.model.*;

/**
 * Created by chekong on 10/11/14.
 */
public class JAuthorizationType implements CanBeSwaggerModel<AuthorizationType> {
    
    private JOAuth oauth2;
    private JApiKey apiKey;

    public JApiKey getApiKey() {
        return apiKey;
    }

    public void setApiKey(JApiKey apiKey) {
        this.apiKey = apiKey;
    }

    public JOAuth getOauth2() {
        return oauth2;
    }

    public void setOauth2(JOAuth oauth2) {
        this.oauth2 = oauth2;
    }

    @Override
    public AuthorizationType toSwaggerModel() {
        if (oauth2 != null) {
            return oauth2.toSwaggerModel();
        }
        if (apiKey != null ) {
            return apiKey.toSwaggerModel();
        }

        return null;

    }
}
