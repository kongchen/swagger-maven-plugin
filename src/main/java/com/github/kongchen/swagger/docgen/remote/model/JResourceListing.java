package com.github.kongchen.swagger.docgen.remote.model;

import java.util.List;

import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.model.ApiListingReference;
import com.wordnik.swagger.model.AuthorizationType;

/**
 * Created by chekong on 10/11/14.
 */
public class JResourceListing   {

    private String apiVersion;
    private String swaggerVersion;
    private List<JApiListingReference> apis;
    private List<JAuthorizationType> authorizations;
    private JApiInfo info;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }

    public List<JApiListingReference> getApis() {
        return apis;
    }

    public void setApis(List<JApiListingReference> apis) {
        this.apis = apis;
    }

    public List<JAuthorizationType> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(List<JAuthorizationType> authorizations) {
        this.authorizations = authorizations;
    }

    public JApiInfo getInfo() {
        return info;
    }

    public void setInfo(JApiInfo info) {
        this.info = info;
    }
}
