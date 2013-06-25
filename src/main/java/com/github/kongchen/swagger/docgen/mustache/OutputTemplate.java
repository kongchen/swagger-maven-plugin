package com.github.kongchen.swagger.docgen.mustache;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

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

    private Set<MustacheDocument> apiDocuments = new LinkedHashSet<MustacheDocument>();

    private Set<MustacheDataType> dataTypes = new LinkedHashSet<MustacheDataType>();

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

    public void addDateType(MustacheDocument mustacheDocument, MustacheDataType dataType) {
        dataTypes.add(dataType);
        for (MustacheItem item : dataType.getItems()) {
            String trueType = TypeUtils.getTrueType(item.getType());
            if (trueType == null) {
                continue;
            }
            addDateType(mustacheDocument, new MustacheDataType(mustacheDocument, trueType));

        }
    }

    public Set<MustacheDocument> getApiDocuments() {
        return apiDocuments;
    }

    public void setApiDocuments(Set<MustacheDocument> apiDocuments) {
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
                addResponseType(mustacheDocument, mustacheOperation.getResponseClass());

            }

            mustacheDocument.addApi(mustacheApi);
        }

        for (String requestType : mustacheDocument.getRequestTypes()) {
            MustacheDataType dataType = new MustacheDataType(mustacheDocument, requestType);

            addDateType(mustacheDocument, dataType);
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
            addDateType(mustacheDocument, dataType);
        }
        filterDatatypes(dataTypes);

        return mustacheDocument;
    }

    private void filterDatatypes(Set<MustacheDataType> dataTypes) {
        Iterator<MustacheDataType> it = dataTypes.iterator();
        while (it.hasNext()){
            MustacheDataType type = it.next();

            if (type.items == null || type.items.size() == 0) {
                it.remove();
            }
        }
    }

    private void addResponseType(MustacheDocument mustacheDocument, MustacheResponseClass responseClass) {
        mustacheDocument.addResponseType(responseClass.getClassLinkName());
        if (responseClass.getGenericClasses() != null) {
            for (MustacheResponseClass mrc : responseClass.getGenericClasses()){
                addResponseType(mustacheDocument, mrc);
            }
        }
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

    public void setDataTypes(Set<MustacheDataType> dataTypes) {
        this.dataTypes = dataTypes;
    }
}
