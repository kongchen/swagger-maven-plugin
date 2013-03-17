package com.github.kongchen.mavenswagger.apigen.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;
import com.wordnik.swagger.core.DocumentationError;
import com.wordnik.swagger.core.DocumentationOperation;
import com.wordnik.swagger.core.DocumentationParameter;
import com.wordnik.swagger.core.DocumentationSchema;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 */
public class MformatApiDocument {
    @JsonIgnore
    private static List<String> basicTypes;

    static {
        String[] a = {
                "string", "boolean", "Date", "int", "Array", "long", "List", "void", "float", "double"
        };
        basicTypes = Arrays.asList(a);
    }

    @JsonIgnore
    private final Documentation swaggerDoc;

    @JsonIgnore
    Pattern pattern = Pattern.compile("^\\w+\\[(\\w+)\\]$");


    int index;

    String resourcePath;

    String description;

    List<MformatApi> apis = new LinkedList<MformatApi>();

    @JsonIgnore
    private Set<String> requestTypes = new LinkedHashSet<String>();

    @JsonIgnore
    private Set<String> responseTypes = new LinkedHashSet<String>();


    public MformatApiDocument(Documentation swaggerDoc) {
        this.swaggerDoc = swaggerDoc;

    }

    public static List<String> getBasicTypes() {
        return basicTypes;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public String getDescription() {
        return description;
    }

    public List<MformatApi> getApis() {
        return apis;
    }

    public Documentation getSwaggerDoc() {
        return swaggerDoc;
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

    private void swaggerDocToMustacheDoc(OutputTemplate outputTemplate, Documentation swaggerDoc) {
        resourcePath = swaggerDoc.getResourcePath();

        int apiIndex = 1;
        for (DocumentationEndPoint api : swaggerDoc.getApis()) {
            description = api.getDescription();
            MformatApi mapi = new MformatApi();
            mapi.apiIndex = apiIndex++;
            addApi(mapi);
            int opIndex = 1;
            for (DocumentationOperation op : api.getOperations()) {
                MformatOperation operation = new MformatOperation();
                operation.opIndex = opIndex++;
                mapi.addOperation(operation);

                operation.httpMethod = op.getHttpMethod();
                mapi.path = api.getPath();
                operation.notes = op.getNotes();
                operation.summary = op.getSummary();
                operation.nickname = op.nickname();
                operation.className = api.path().split("/")[1].split("\\.")[0].replaceAll("/", "");


                mapi.url = swaggerDoc.getBasePath() + api.getPath();
                if (op.getParameters() != null) {
                    operation.parameters = writeParameters(op.getParameters());
                }

//                sb.append(hc0(level, "Parameters", writeParameters(op.getParameters())));

                String trueType = getTrueType(op.getResponseClass());
                if(trueType != null){
                    responseTypes.add(trueType);
                    operation.responseClass = op.getResponseClass();
                    operation.responseClassLinkType = trueType;
                }

                operation.errorResponses = op.getErrorResponses();
            }
        }

        for (String classname : requestTypes) {
            MformatDataType dataType = new MformatDataType();
            dataType.name = (classname);
            dataType.items = getItemsFromClass(classname);
            outputTemplate.addDateType(dataType);
        }

        Set<String> typelist = new LinkedHashSet<String>();

        for (String classname : responseTypes) {
            if (!requestTypes.contains(classname)) {
                String ttype = getTrueType(classname);
                if(ttype!=null){
                    typelist.add(ttype);
                }
            }
        }

        for (String classname : typelist) {
            MformatDataType dataType = new MformatDataType();
            dataType.name = (classname);
            dataType.items = getItemsFromClass(classname);
            outputTemplate.addDateType(dataType);

        }

    }

    private List<MformatParaSet> writeParameters(List<DocumentationParameter> parameters) {
        List<MformatParaSet> list = new LinkedList<MformatParaSet>();

        Map<String, List<MformatPara>> paraMap = new HashMap<String, List<MformatPara>>();

        for (DocumentationParameter para : parameters) {
            if (!paraMap.containsKey(para.getParamType())) {
                List<MformatPara> paraList = new LinkedList<MformatPara>();
                paraMap.put(para.getParamType(), paraList);
            }
            List<MformatPara> paraList = paraMap.get(para.getParamType());

            MformatPara info = new MformatPara();
            info.linkType = getTrueType(para.getDataType());

            if (swaggerDoc.getModels().get(info.linkType) == null) {
                info.type = para.getDataType();
                info.name = para.getName();
            } else {
                requestTypes.add(info.linkType);
                info.type = para.getDataType();
                info.name = para.getDataType();
            }

            info.required = para.required();
            info.description = para.getDescription();
            paraList.add(info);
        }

        for (String type : paraMap.keySet()) {
            MformatParaSet wParameters = new MformatParaSet();
            wParameters.paramType = type;
            wParameters.paras = paraMap.get(type);
            list.add(wParameters);


        }


        return list;

    }

    private String getTrueType(String dataType) {
        String t;
        Matcher m = pattern.matcher(dataType);
        if(m.find()){
            t = m.group(1);

        }else{
            t = dataType;
        }
        if(basicTypes.contains(t)){
            t = null;
        }

        return t;
    }

    private List<MformatItem> getItemsFromClass(String responseClass) {

        if (responseClass.equals("void")) {
            return null;
        }

        List<MformatItem> ItemList = new LinkedList<MformatItem>();
        DocumentationSchema field = swaggerDoc.getModels().get(responseClass);

        if (field != null && field.getProperties() != null) {
            for (String name : field.getProperties().keySet()) {
                DocumentationSchema prop = field.getProperties().get(name);
                DocumentationSchema item = prop.getItems();


                MformatItem fi = new MformatItem();
                fi.name = name;
                fi.type = prop.getType();
                fi.linkType = fi.type;
                if (swaggerDoc.getModels().get(fi.type) != null) {
                    responseTypes.add(fi.type);
                } else if (fi.type.equalsIgnoreCase("Array")) {
                    if (item != null) {
                        if (item.getType().equals("any") && item.ref() != null) {
                            fi.type = "Array:" + item.ref() + "";
                            fi.linkType = item.ref();
                            responseTypes.add(item.ref());
                        } else {
                            fi.type = "Array:" + item.getType() + "";
                            fi.linkType = item.getType();
                        }
                    }
                }
                fi.required = prop.required();
                fi.access = prop.getAccess();
                fi.description = prop.getDescription();
                fi.notes = prop.getNotes();
                fi.linkType = filterBasicTypes(fi.linkType);

                ItemList.add(fi);


            }

        }
        return ItemList;
    }

    private String filterBasicTypes(String linkType) {
        if (basicTypes.contains(linkType)) {
            return null;
        }
        return linkType;
    }

    public void addApi(MformatApi wapi) {
        apis.add(wapi);
    }

    public void addTo(OutputTemplate outputTemplate) {
        swaggerDocToMustacheDoc(outputTemplate, swaggerDoc);
        this.setIndex(outputTemplate.getApiDocuments().size() + 1);
        outputTemplate.getApiDocuments().add(this);
    }
}

class MformatApi {
    int apiIndex;

    String path;

    String url;

    List<MformatOperation> operations = new LinkedList<MformatOperation>();

    public void addOperation(MformatOperation operation) {
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

    public List<MformatOperation> getOperations() {
        return operations;
    }

    public void setOperations(List<MformatOperation> operations) {
        this.operations = operations;
    }
}

class MformatOperation {
    int opIndex;

    String httpMethod;

    String summary;

    String notes;

    String responseClass;

    String nickname;

    String className;

    List<MformatParaSet> parameters;

    List<DocumentationError> errorResponses;

    String responseClassLinkType;


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

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<MformatParaSet> getParameters() {
        return parameters;
    }

    public void setParameters(List<MformatParaSet> parameters) {
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

class MformatParaSet {
    String paramType;

    List<MformatPara> paras;

    public String getParamType() {
        return paramType;
    }

    public void setParamType(String paramType) {
        this.paramType = paramType;
    }

    public List<MformatPara> getParas() {
        return paras;
    }

    public void setParas(List<MformatPara> paras) {
        this.paras = paras;
    }
}

class MformatPara {
    String name;

    boolean required;

    String description;

    String type;

    String linkType;

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

class MformatDataType {
    String name;

    List<MformatItem> items;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MformatDataType)) return false;

        MformatDataType that = (MformatDataType) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MformatItem> getItems() {
        return items;
    }

    public void setItems(List<MformatItem> items) {
        this.items = items;
    }
}

class MformatItem {
    String name;

    String type;

    String linkType;

    boolean required;

    String access;

    String description;

    String notes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

