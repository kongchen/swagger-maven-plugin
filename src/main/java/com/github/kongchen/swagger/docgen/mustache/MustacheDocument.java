package com.github.kongchen.swagger.docgen.mustache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.kongchen.swagger.docgen.TypeUtils;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.Documentation;
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
            if (mustacheParameter.getLinkType() != null
                    && !para.getParamType().equals(ApiValues.TYPE_HEADER)){
                requestTypes.add(mustacheParameter.getLinkType());
            }
            if (para.getName() != null) {
                mustacheParameter.setName(para.getName());
            } else {
                mustacheParameter.setName(para.getDataType());
            }
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
        if (o == null) {
            return 1;
        }
        return this.getResourcePath().compareTo(o.getResourcePath());
    }
}

