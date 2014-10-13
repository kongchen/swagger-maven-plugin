package com.github.kongchen.swagger.docgen.remote.model;

import java.util.ArrayList;
import java.util.List;

import com.github.kongchen.swagger.docgen.remote.ListConverter;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.AuthorizationScope;
import com.wordnik.swagger.model.GrantType;
import com.wordnik.swagger.model.OAuth;

/**
 * Created by chekong on 10/11/14.
 */

public class JOAuth implements CanBeSwaggerModel<OAuth> {
    private String type;
    private List<JAuthorizationScope> scopes;
    private List<JGrantType> grantTypes;

    
    public String getType() {
        return type;
    }

    
    public void setType(String type) {
        this.type = type;
    }

    public List<JAuthorizationScope> getScopes() {
        return scopes;
    }

    public void setScopes(List<JAuthorizationScope> scopes) {
        this.scopes = scopes;
    }

    public List<JGrantType> getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(List<JGrantType> grantTypes) {
        this.grantTypes = grantTypes;
    }

    @Override
    public OAuth toSwaggerModel() {
        List<GrantType> gtypes = new ArrayList<GrantType>();
        for (JGrantType jgt : grantTypes) {
            gtypes.add(jgt.getAuthorization_code().toSwaggerModel());
            gtypes.add(jgt.getImplicit().toSwaggerModel());
        }
        return new OAuth(
                new ListConverter<JAuthorizationScope, AuthorizationScope>(scopes).convert(),
                Utils.toScalaImmutableList(gtypes));
    }
}
