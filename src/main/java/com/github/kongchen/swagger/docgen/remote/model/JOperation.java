package com.github.kongchen.swagger.docgen.remote.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.kongchen.swagger.docgen.remote.ListConverter;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;

import java.util.List;


/**
 * Created by chekong on 10/11/14.
 */
public class JOperation implements CanBeSwaggerModel<Operation> {
    private String method;
    private String summary;
    private String notes;
    @JsonProperty("type")
    private String responseClass;
    private String nickname;
    private int position;
    private List<String> produces;
    private List<String> consumes;
    private List<String> protocols;
    private List<JAuthorization> authorizations;
    private List<JParameter> parameters;
    private List<JResponseMessage> responseMessages;
    private String deprecated;
    private JModelRef items;

    public JModelRef getItems() {
        return items;
    }

    public void setItems(JModelRef items) {
        this.items = items;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setResponseClass(String responseClass) {
        this.responseClass = responseClass;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPosition(int position) {
        this.position = position;
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

    public void setParameters(List<JParameter> parameters) {
        this.parameters = parameters;
    }

    public void setResponseMessages(List<JResponseMessage> responseMessages) {
        this.responseMessages = responseMessages;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public String getMethod() {
        return method;
    }

    public String getSummary() {
        return summary;
    }

    public String getNotes() {
        return notes;
    }

    public String getResponseClass() {
        return responseClass;
    }

    public String getNickname() {
        return nickname;
    }

    public int getPosition() {
        return position;
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

    public List<JAuthorization> getAuthorizations() {
        return authorizations;
    }

    public List<JParameter> getParameters() {
        return parameters;
    }

    public List<JResponseMessage> getResponseMessages() {
        return responseMessages;
    }

    public String getDeprecated() {
        return deprecated;
    }

    @Override
    public Operation toSwaggerModel() {
        if (items != null && this.getResponseClass().equalsIgnoreCase("array")) {
            responseClass = "List["+this.items.getRef()+"]";

        } 
        return new Operation(method, summary, notes, responseClass, nickname, position,
                Utils.toScalaImmutableList(produces), Utils.toScalaImmutableList(consumes),
                Utils.toScalaImmutableList(protocols),
                new ListConverter<JAuthorization, Authorization>(authorizations).convert(),
                new ListConverter<JParameter, Parameter>(parameters).convert(),
                new ListConverter<JResponseMessage, ResponseMessage>(responseMessages).convert(),
                Utils.getOption(deprecated));
    }
}
