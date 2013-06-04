package com.github.kongchen.swagger.docgen.mustache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.kongchen.swagger.docgen.TypeUtils;
import static com.github.kongchen.swagger.docgen.TypeUtils.getTrueType;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationError;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationParameter;
import com.wordnik.swagger.core.DocumentationSchema;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */
public class MustacheDocument implements Comparable<MustacheDocument>{
    protected static final String VOID = "void";

    protected static final String ARRAY = "Array";

    protected static final String ANY = "any";

    @JsonIgnore
    private final HashMap<String, DocumentationSchema> models;

    private int index;

    String resourcePath;

    String description;

    List<MustacheApi> apis = new LinkedList<MustacheApi>();

    @JsonIgnore
    private Set<String> requestTypes = new LinkedHashSet<String>();

    @JsonIgnore
    private Set<String> responseTypes = new LinkedHashSet<String>();

    @JsonIgnore
    private int apiIndex = 1;

    public MustacheDocument(Documentation swaggerDoc) {
        this.models = swaggerDoc.getModels();
        this.resourcePath = swaggerDoc.getResourcePath();
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getDescription() {
        return description;
    }

    public List<MustacheApi> getApis() {
        return apis;
    }

    public Set<String> getRequestTypes() {
        return requestTypes;
    }

    public Set<String> getResponseTypes() {
        return responseTypes;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void addApi(MustacheApi wapi) {
        wapi.apiIndex = apiIndex++;
        apis.add(wapi);
    }

    public void addResponseType(String trueType) {
        if (trueType != null) {
            responseTypes.add(trueType);
        }
    }

    public List<MustacheParameterSet> analyzeParameters(List<DocumentationParameter> parameters) {
        if (parameters == null) return null;
        List<MustacheParameterSet> list = new LinkedList<MustacheParameterSet>();

        Map<String, List<MustacheParameter>> paraMap = toParameterTypeMap(parameters);

        for (Map.Entry<String, List<MustacheParameter>> entry : paraMap.entrySet()) {
            list.add(new MustacheParameterSet(entry));
        }

        return list;
    }

    private Map<String, List<MustacheParameter>> toParameterTypeMap(List<DocumentationParameter> parameters) {
        Map<String, List<MustacheParameter>> paraMap = new HashMap<String, List<MustacheParameter>>();

        for (DocumentationParameter para : parameters) {
            MustacheParameter mustacheParameter = analyzeParameter(para);

            List<MustacheParameter> paraList = paraMap.get(para.getParamType());
            if (paraList == null) {
                paraList = new LinkedList<MustacheParameter>();
                paraMap.put(para.getParamType(), paraList);
            }

            paraList.add(mustacheParameter);
        }
        return paraMap;
    }

    private MustacheParameter analyzeParameter(DocumentationParameter para) {
        MustacheParameter mustacheParameter = new MustacheParameter(para);

        if (models != null && models.get(mustacheParameter.linkType) == null) {
            mustacheParameter.setName(para.getName());
        } else {
            if (mustacheParameter.getLinkType() != null){
                requestTypes.add(mustacheParameter.getLinkType());
            }
            mustacheParameter.setName(para.getDataType());
        }

        return mustacheParameter;
    }

    public List<MustacheItem> analyzeDataTypes(String responseClass) {
        if (responseClass == null || responseClass.equals(VOID) || models == null) {
            return null;
        }

        List<MustacheItem> mustacheItemList = new LinkedList<MustacheItem>();
        DocumentationSchema field = models.get(TypeUtils.upperCaseFirstCharacter(responseClass));

        if (field != null && field.getProperties() != null) {
            for (Map.Entry<String, DocumentationSchema> entry : field.getProperties().entrySet()) {
                MustacheItem mustacheItem = new MustacheItem(entry.getKey(), entry.getValue());

                DocumentationSchema item = entry.getValue().getItems();

                if (models.get(mustacheItem.getType()) != null) {
                    responseTypes.add(mustacheItem.getType());
                } else if (mustacheItem.getType().equalsIgnoreCase(ARRAY)) {
                    handleArrayType(mustacheItem, item);
                }

                mustacheItemList.add(mustacheItem);
            }
        }
        return mustacheItemList;
    }

    private void handleArrayType(MustacheItem mustacheItem, DocumentationSchema item) {
        if (item != null) {
            if (item.getType().equals(ANY) && item.ref() != null) {
                mustacheItem.setTypeAsArray(item.ref());
                responseTypes.add(item.ref());
            } else {
                mustacheItem.setTypeAsArray(item.getType());
            }
        }
    }

    @Override
    public int compareTo(MustacheDocument o) {
        return this.getIndex() - o.getIndex();
    }
}

class MustacheApi {
    int apiIndex;

    String path;

    String url;

    List<MustacheOperation> operations = new LinkedList<MustacheOperation>();

    @JsonIgnore
    private int opIndex = 1;

    public MustacheApi(String basePath, DocumentationEndPoint api) {
        this.path = api.getPath();
        if (this.path != null && !this.path.startsWith("/")) {
            this.path = "/" + this.path;
        }
        this.url = basePath + api.getPath();
    }

    public void addOperation(MustacheOperation operation) {
        operation.setOpIndex(this.opIndex++);
        operations.add(operation);
    }

    public int getApiIndex() {
        return apiIndex;
    }

    public void setApiIndex(int apiIndex) {
        this.apiIndex = apiIndex;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<MustacheOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<MustacheOperation> operations) {
        this.operations = operations;
    }
}

class MustacheOperation {
    int opIndex;

    String httpMethod;

    String summary;

    String notes;

    String responseClass;

    String nickname;

    List<MustacheParameterSet> parameters;

    List<DocumentationError> errorResponses;

    String responseClassLinkType;

    public MustacheOperation(MustacheDocument mustacheDocument, DocumentationOperation op) {
        this.httpMethod = op.getHttpMethod();
        this.notes = op.getNotes();
        this.summary = op.getSummary();
        this.nickname = op.nickname();
        this.parameters = mustacheDocument.analyzeParameters(op.getParameters());
        String trueType = TypeUtils.getTrueType(op.getResponseClass());
        if (trueType != null) {
            this.responseClass = op.getResponseClass();
            this.responseClassLinkType = trueType;
        }

        this.errorResponses = op.getErrorResponses();
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

    public String getResponseClass() {
        return responseClass;
    }

    public void setResponseClass(String responseClass) {
        this.responseClass = responseClass;
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

    public String getResponseClassLinkType() {
        return responseClassLinkType;
    }

    public void setResponseClassLinkType(String responseClassLinkType) {
        this.responseClassLinkType = responseClassLinkType;
    }
}

class MustacheParameterSet {
    String paramType;

    List<MustacheParameter> paras;

    public MustacheParameterSet(Map.Entry<String, List<MustacheParameter>> entry) {
        this.paramType = entry.getKey();
        this.paras = entry.getValue();
    }

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public List<MustacheParameter> getParas() {
        return paras;
    }

    public void setParas(List<MustacheParameter> paras) {
        this.paras = paras;
    }
}

class MustacheParameter {
    String name;

    boolean required;

    String description;

    String type;

    String linkType;

    public MustacheParameter(DocumentationParameter para) {
        this.linkType = getTrueType(para.getDataType());
        this.required = para.required();
        this.description = para.getDescription();
        this.type = para.getDataType();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLinkType() {
        return linkType;
    }

    public void setLinkType(String linkType) {
        this.linkType = linkType;
    }
}

