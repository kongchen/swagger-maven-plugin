package com.github.kongchen.swagger.docgen.remote.model;

import com.github.kongchen.swagger.docgen.remote.ListConverter;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.Model;
import scala.Option;
import scala.Predef;
import scala.collection.JavaConversions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by chekong on 10/11/14.
 */
public class JApiListing implements CanBeSwaggerModel<ApiListing>{
    private String apiVersion;
    private String swaggerVersion;
    private String basePath;
    private String resourcePath;
    private List<String> produces;
    private List<String> consumes;
    private List<String> protocols;
    private List<JAuthorization> authorizations;
    private List<JApiDescription> apis;
    private Map<String, JModel> models;
    private String description;
    private int position;

    public String getApiVersion() {
        return apiVersion;
    }

    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public List<String> getProduces() {
        return produces;
    }

    public List<String> getConsumes() {
        return consumes;
    }

    public List<String> getProtocols() {
        return protocols;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public void setProtocols(List<String> protocols) {
        this.protocols = protocols;
    }

    public void setAuthorizations(List<JAuthorization> authorizations) {
        this.authorizations = authorizations;
    }

    public void setApis(List<JApiDescription> apis) {
        this.apis = apis;
    }

    public void setModels(Map<String, JModel> models) {
        this.models = models;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public List<JAuthorization> getAuthorizations() {
        return authorizations;
    }

    public List<JApiDescription> getApis() {
        return apis;
    }

    public Map<String, JModel> getModels() {
        return models;
    }

    public String getDescription() {
        return description;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public ApiListing toSwaggerModel() {
        JApiListing doc = this;

        HashMap<String, Model> _models = new HashMap<String, Model>();
        for (String key: doc.getModels().keySet()) {
            _models.put(key, doc.getModels().get(key).toSwaggerModel());
        }

        return new ApiListing(doc.getApiVersion(), doc.getSwaggerVersion(), doc.getBasePath(), doc.getResourcePath(),
                Utils.toScalaImmutableList(doc.getProduces()), Utils.toScalaImmutableList(doc.getConsumes()),
                Utils.toScalaImmutableList(doc.getProtocols()),
                new ListConverter<JAuthorization, Authorization>(doc.getAuthorizations()).convert(),
                new ListConverter<JApiDescription, ApiDescription>(doc.getApis()).convert(),
                Option.apply(Utils.toScalaImmutableMap(_models)), Option.apply(doc.getDescription()), doc.getPosition());
    }
}
