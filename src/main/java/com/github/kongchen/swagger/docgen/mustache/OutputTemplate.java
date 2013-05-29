package com.github.kongchen.swagger.docgen.mustache;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.TypeUtils;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationOperation;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
public class OutputTemplate {
    private String basePath;

    private String apiVersion;

    private List<MustacheDocument> apiDocuments = new LinkedList<MustacheDocument>();

    private Set<MustacheDataType> dataTypes = new TreeSet<MustacheDataType>();

    public OutputTemplate(AbstractDocumentSource docSource) throws Exception {
        feedSource(docSource);
    }

    public static String getJsonSchema() {
        ObjectMapper m = new ObjectMapper();
        try {
            JsonSchema js = m.generateJsonSchema(OutputTemplate.class);
            return m.writeValueAsString(js);
        } catch (Exception e) {
            return null;
        }
    }

    public Set<MustacheDataType> getDataTypes() {
        return dataTypes;
    }

    public void addDateType(MustacheDataType dataType) {
        dataTypes.add(dataType);
    }

    public List<MustacheDocument> getApiDocuments() {
        return apiDocuments;
    }

    public void setApiDocuments(List<MustacheDocument> apiDocuments) {
        this.apiDocuments = apiDocuments;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    private MustacheDocument createMustacheDocument(Documentation swaggerDoc) {
        MustacheDocument mustacheDocument = new MustacheDocument(swaggerDoc);

        for (DocumentationEndPoint api : swaggerDoc.getApis()) {
            mustacheDocument.setDescription(api.getDescription());

            MustacheApi mustacheApi = new MustacheApi(swaggerDoc.getBasePath(), api);

            for (DocumentationOperation op : api.getOperations()) {
                MustacheOperation mustacheOperation = new MustacheOperation(mustacheDocument, op);
                mustacheApi.addOperation(mustacheOperation);
                mustacheDocument.addResponseType(mustacheOperation.getResponseClassLinkType());
            }

            mustacheDocument.addApi(mustacheApi);
        }

        for (String requestType : mustacheDocument.getRequestTypes()) {
            MustacheDataType dataType = new MustacheDataType(mustacheDocument, requestType);

            addDateType(dataType);
        }

        Set<String> missedTypes = new LinkedHashSet<String>();

        for (String responseType : mustacheDocument.getResponseTypes()) {
            if (!mustacheDocument.getRequestTypes().contains(responseType)) {
                String ttype = TypeUtils.getTrueType(responseType);
                if (ttype != null) {
                    missedTypes.add(ttype);
                }
            }
        }

        for (String type : missedTypes) {
            MustacheDataType dataType = new MustacheDataType(mustacheDocument, type);
            addDateType(dataType);
        }

        return mustacheDocument;
    }

    private void feedSource(AbstractDocumentSource source) throws Exception {
        for (Documentation doc : source.getValidDocuments()) {
            if (doc.getApis() ==null ){
                continue;
            }
            MustacheDocument mustacheDocument = createMustacheDocument(doc);
            addMustacheDocument(mustacheDocument);
        }
        setBasePath(source.getBasePath());
        setApiVersion(source.getApiVersion());
    }

    private void addMustacheDocument(MustacheDocument mustacheDocument) {
        mustacheDocument.setIndex(apiDocuments.size() + 1);
        apiDocuments.add(mustacheDocument);
    }
}
