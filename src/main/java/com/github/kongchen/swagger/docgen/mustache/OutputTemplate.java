package com.github.kongchen.swagger.docgen.mustache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.TypeUtils;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Operation;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 3/7/13
 */
public class OutputTemplate {
    private String basePath;

    private String apiVersion;

    private List<MustacheDocument> apiDocuments = new ArrayList<MustacheDocument>();

    private Set<MustacheDataType> dataTypes = new TreeSet<MustacheDataType>();

    public OutputTemplate(AbstractDocumentSource docSource) throws GenerateException {
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
        if (dataTypes.contains(dataType)) {
            return;
        }
        dataTypes.add(dataType);
        for (MustacheItem item : dataType.getItems()) {
            String trueType = TypeUtils.getTrueType(item.getType());
            if (trueType == null) {
                continue;
            }
            addDateType(mustacheDocument, new MustacheDataType(mustacheDocument, trueType));

        }
    }

    public List<MustacheDocument> getApiDocuments() {
        return apiDocuments;
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

    /**
     * Create mustache document according to a swagger document apilisting
     * @param swaggerDoc
     * @return
     */
    private MustacheDocument createMustacheDocument(ApiListing swaggerDoc) throws GenerateException {
        MustacheDocument mustacheDocument = new MustacheDocument(swaggerDoc);

        setApiVersion(swaggerDoc.apiVersion());
        setBasePath(swaggerDoc.basePath());
        for (scala.collection.Iterator<ApiDescription> it = swaggerDoc.apis().iterator(); it.hasNext(); ) {
            ApiDescription api = it.next();
            mustacheDocument.setDescription(Utils.getStrInOption(api.description()));

            MustacheApi mustacheApi = new MustacheApi(swaggerDoc.basePath(), api);

            for (scala.collection.Iterator<Operation> opIt  = api.operations().iterator(); opIt.hasNext(); ) {
                Operation op = opIt.next();
                MustacheOperation mustacheOperation = null;
                try {
                    mustacheOperation = new MustacheOperation(mustacheDocument, op);
                } catch (GenerateException e) {
                    throw new GenerateException("Parse failed in "+op.toString() + " Error:" + e.getMessage());
                }
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

            if (type.getItems() == null || type.getItems().size() == 0) {
                it.remove();
            }
        }
    }

    private void addResponseType(MustacheDocument mustacheDocument, MustacheResponseClass responseClass) {
        mustacheDocument.addResponseType(responseClass);
        if (responseClass.getGenericClasses() != null) {
            for (MustacheResponseClass mrc : responseClass.getGenericClasses()){
                addResponseType(mustacheDocument, mrc);
            }
        }
    }

    private void feedSource(AbstractDocumentSource source) throws GenerateException {
        for (ApiListing doc : source.getValidDocuments()) {
            if (doc.apis().isEmpty()){
                continue;
            }
            MustacheDocument mustacheDocument = createMustacheDocument(doc);
            addMustacheDocument(mustacheDocument);
        }
        Collections.sort(apiDocuments, new Comparator<MustacheDocument>() {
            @Override
            public int compare(MustacheDocument o1, MustacheDocument o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
    }

    private void addMustacheDocument(MustacheDocument mustacheDocument) {
        apiDocuments.add(mustacheDocument);
    }

    public void setDataTypes(Set<MustacheDataType> dataTypes) {
        this.dataTypes = dataTypes;
    }
}
