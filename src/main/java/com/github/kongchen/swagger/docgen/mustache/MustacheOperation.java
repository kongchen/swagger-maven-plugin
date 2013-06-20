package com.github.kongchen.swagger.docgen.mustache;

import java.util.Iterator;
import java.util.List;

import com.github.kongchen.swagger.docgen.DocTemplateConstants;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.DocumentationError;
import com.wordnik.swagger.core.DocumentationOperation;

public class MustacheOperation {
    int opIndex;

    String httpMethod;

    String summary;

    String notes;

    MustacheResponseClass responseClass;

    String nickname;

    List<MustacheParameterSet> parameters;

    MustacheParameterSet requestQuery;
    MustacheParameterSet requestHeader;
    MustacheParameterSet requestBody;
    MustacheParameterSet requestPath;
    MustacheParameterSet responseHeader;

    List<DocumentationError> errorResponses;

    List<MustacheSample> samples;

    public MustacheOperation(MustacheDocument mustacheDocument, DocumentationOperation op) {
        this.httpMethod = op.getHttpMethod();
        this.notes = op.getNotes();
        this.summary = op.getSummary();
        this.nickname = op.nickname();
        this.parameters = mustacheDocument.analyzeParameters(op.getParameters());
        responseClass = new MustacheResponseClass(op.getResponseClass());
        this.errorResponses = op.getErrorResponses();
        Iterator<MustacheParameterSet> it = parameters.iterator();
        while (it.hasNext()) {
            MustacheParameterSet para = it.next();
            if (para.getParamType().equals(ApiValues.TYPE_QUERY)) {
                this.requestQuery = para;
                it.remove();
            } else if (para.getParamType().equals(ApiValues.TYPE_HEADER)) {
                this.requestHeader = para;
                it.remove();
            } else if (para.getParamType().equals(ApiValues.TYPE_BODY)) {
                this.requestBody = para;
                it.remove();
            } else if (para.getParamType().equals(ApiValues.TYPE_PATH)) {
                this.requestPath = para;
                it.remove();
            } else if (para.getParamType().equals(DocTemplateConstants.TYPE_RESPONSE_HEADER)) {
                this.responseHeader = para;
                it.remove();
            }
        }
    }

    public MustacheParameterSet getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(MustacheParameterSet responseHeader) {
        this.responseHeader = responseHeader;
    }

    public MustacheParameterSet getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(MustacheParameterSet requestPath) {
        this.requestPath = requestPath;
    }

    public MustacheParameterSet getRequestQuery() {
        return requestQuery;
    }

    public void setRequestQuery(MustacheParameterSet requestQuery) {
        this.requestQuery = requestQuery;
    }

    public MustacheParameterSet getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(MustacheParameterSet requestHeader) {
        this.requestHeader = requestHeader;
    }

    public MustacheParameterSet getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(MustacheParameterSet requestBody) {
        this.requestBody = requestBody;
    }

    public List<MustacheSample> getSamples() {
        return samples;
    }

    public void setSamples(List<MustacheSample> samples) {
        this.samples = samples;
    }

    public int getOpIndex() {
        return opIndex;
    }

    public void setOpIndex(int opIndex) {
        this.opIndex = opIndex;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<MustacheParameterSet> getParameters() {
        return parameters;
    }

    public void setParameters(List<MustacheParameterSet> parameters) {
        this.parameters = parameters;
    }

    public List<DocumentationError> getErrorResponses() {
        return errorResponses;
    }

    public void setErrorResponses(List<DocumentationError> errorResponses) {
        this.errorResponses = errorResponses;
    }

    public MustacheResponseClass getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(MustacheResponseClass responseClass) {
        this.responseClass = responseClass;
    }
}
