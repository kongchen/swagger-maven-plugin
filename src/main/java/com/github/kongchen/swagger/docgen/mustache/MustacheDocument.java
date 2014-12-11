package com.github.kongchen.swagger.docgen.mustache;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.kongchen.swagger.docgen.DocTemplateConstants;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.util.ModelUtil;
import com.wordnik.swagger.model.*;
import scala.Option;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import scala.collection.mutable.LinkedEntry;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */
public class MustacheDocument implements Comparable<MustacheDocument> {
    protected static final String VOID = "void";

    protected static final String ARRAY = "Array";

    private static final String LIST = "List";

    @JsonIgnore
    private final Map<String, Model> models = new HashMap<String, Model>();

    private int index;

    private String resourcePath;

    private String description;

    private List<MustacheApi> apis = new ArrayList<MustacheApi>();

    private List<MustacheContentType> responseContentTypes = new ArrayList<MustacheContentType>();

    private List<MustacheContentType> parameterContentTypes = new ArrayList<MustacheContentType>();
    
    @JsonIgnore
    private Set<String> requestTypes = new LinkedHashSet<String>();

    @JsonIgnore
    private Set<String> responseTypes = new LinkedHashSet<String>();

    @JsonIgnore
    private int apiIndex = 1;

    @JsonIgnore
    private static final Map<String, Integer> hashValueMap = new HashMap<String, Integer>();

    static {
        hashValueMap.put(ApiValues.TYPE_HEADER(), 1);
        hashValueMap.put(ApiValues.TYPE_PATH(), 2);
        hashValueMap.put(ApiValues.TYPE_QUERY(), 3);
        hashValueMap.put(ApiValues.TYPE_BODY(), 4);
        hashValueMap.put(ApiValues.TYPE_FORM(), 5);
        hashValueMap.put(ApiValues.TYPE_COOKIE(), 6);
        hashValueMap.put(ApiValues.TYPE_MATRIX(), 7);
        hashValueMap.put(DocTemplateConstants.TYPE_RESPONSE_HEADER, 8);
    }

    public MustacheDocument(ApiListing apiListing) {
        if (!apiListing.models().isEmpty()) {
            models.putAll(JavaConversions.mapAsJavaMap(apiListing.models().get()));
        }
        this.resourcePath = apiListing.resourcePath();
        this.index = apiListing.position();
        this.apis = new ArrayList<MustacheApi>(apiListing.apis().size());
        this.description = Utils.getStrInOption(apiListing.description());
        this.responseContentTypes = new ArrayList<MustacheContentType>(apiListing.produces().size());
        this.parameterContentTypes = new ArrayList<MustacheContentType>(apiListing.consumes().size());
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

    public List<MustacheContentType> getResponseContentTypes() {
        return responseContentTypes;
    }

    public List<MustacheContentType> getParameterContentTypes() {
        return parameterContentTypes;
    }
    
    public void setIndex(int index) {
        this.index = index;
    }

    public void setApis(List<MustacheApi> apis) {
      this.apis = apis;
    }

    public void addResponseType(MustacheResponseClass clz) {
        if (clz.getClassName() == null) {
            return;
        }
        String newName = addModels(JavaConversions.mapAsJavaMap(ModelUtil.modelAndDependencies(clz.getClassName())));
        if (newName == null) {
            responseTypes.add(clz.getClassLinkName());
            return;
        }
        if (newName.equals(clz.getClassLinkName())) {
            responseTypes.add(newName);
        }

    }

    public void addResponseContentTypes(MustacheContentType responseContentTypes) {
        this.responseContentTypes.add(responseContentTypes);
    }

    public void addParameterContentTypes(MustacheContentType parameterContentTypes) {
        this.parameterContentTypes.add(parameterContentTypes);
    }

    public List<MustacheParameterSet> analyzeParameters(List<Parameter> parameters) {
        if (parameters == null) return null;
        List<MustacheParameterSet> parameterList = new ArrayList<MustacheParameterSet>();

        Map<String, List<MustacheParameter>> paraMap = toParameterTypeMap(parameters);

        for (Map.Entry<String, List<MustacheParameter>> entry : paraMap.entrySet()) {
            parameterList.add(new MustacheParameterSet(entry));
        }

        // make sure parameter order is 1.header 2.path 3.query 4.body 5.response header
        Collections.sort(parameterList, new Comparator<MustacheParameterSet>() {

            @Override
            public int compare(MustacheParameterSet o1, MustacheParameterSet o2) {
                return hashValue(o1) - hashValue(o2);
            }

            private int hashValue(MustacheParameterSet parameterSet) {
                if (parameterSet == null || parameterSet.getParamType() == null) {
                    return 0;
                } else {
                    return hashValueMap.get(parameterSet.getParamType());
                }
            }
        });
        return parameterList;
    }

    private Map<String, List<MustacheParameter>> toParameterTypeMap(List<Parameter> parameters) {
        Map<String, List<MustacheParameter>> paraMap = new HashMap<String, List<MustacheParameter>>();

        for (Parameter para : parameters) {
            MustacheParameter mustacheParameter = analyzeParameter(para);

            List<MustacheParameter> paraList = paraMap.get(para.paramType());
            if (paraList == null) {
                paraList = new LinkedList<MustacheParameter>();
                paraMap.put(para.paramType(), paraList);
            }

            paraList.add(mustacheParameter);
        }
        return paraMap;
    }

    private MustacheParameter analyzeParameter(Parameter para) {
        MustacheParameter mustacheParameter = null;
        mustacheParameter = new MustacheParameter(para);

        if (models.get(mustacheParameter.getLinkType()) == null) {
            mustacheParameter.setName(para.name());
        } else {
            if (mustacheParameter.getLinkType() != null) {
                requestTypes.add(mustacheParameter.getLinkType());
            }
            if (para.name() != null) {
                mustacheParameter.setName(para.name());
            } else {
                mustacheParameter.setName(para.dataType());
            }
        }

        return mustacheParameter;
    }

    public List<MustacheItem> analyzeDataTypes(String responseClass) {
        List<MustacheItem> mustacheItemList = new ArrayList<MustacheItem>();
        if (responseClass == null || responseClass.equals(VOID)) {
            return mustacheItemList;
        }

        Model field = models.get(responseClass);
        if (field != null && field.properties() != null) {

            for (Iterator<LinkedEntry<String, ModelProperty>> it = field.properties().entriesIterator(); it.hasNext(); ) {
                LinkedEntry<String, ModelProperty> entry = it.next();
                MustacheItem mustacheItem = new MustacheItem(entry.key(), entry.value());

                Option<ModelRef> itemOption = entry.value().items();
                ModelRef item = itemOption.isEmpty() ? null : itemOption.get();

                if (mustacheItem.getType().equalsIgnoreCase(ARRAY)
                    || mustacheItem.getType().equalsIgnoreCase(LIST)) {
                    handleArrayType(mustacheItem, item);
                } else if (models.get(mustacheItem.getType()) != null) {
                    responseTypes.add(mustacheItem.getType());
                }

                mustacheItemList.add(mustacheItem);
            }
        }
        Collections.sort(mustacheItemList, new Comparator<MustacheItem>() {
            @Override
            public int compare(MustacheItem o1, MustacheItem o2) {
                if (o1 != null && o2 != null) {
                    return o1.getPosition() - o2.getPosition();
                } else {
                    return 0;
                }
            }
        });
        return mustacheItemList;
    }

    private void handleArrayType(MustacheItem mustacheItem, ModelRef item) {
        if (item != null) {
            if (item.type() == null && item.ref() != null) {
                mustacheItem.setTypeAsArray(Utils.getStrInOption(item.ref()));
                responseTypes.add(Utils.getStrInOption(item.ref()));
            } else {
                mustacheItem.setTypeAsArray(item.type());
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

    public String addModels(Map<String, Model> modelMap) {
        if (modelMap == null || modelMap.isEmpty()) {
            return null;
        }
        for (String key : modelMap.keySet()) {
            models.put(key, modelMap.get(key));
        }
        return modelMap.keySet().iterator().next();
    }
}

