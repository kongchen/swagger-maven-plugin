package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.google.common.base.CharMatcher;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.core.ApiValues;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.model.*;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.mutable.LinkedHashMap;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author tedleman
 *         <p/>
 *         The use-goal of this object is to return an ApiListing object from the read() method.
 *         The listing object is populated with other api objects, contained by ApiDescriptions
 *         <p/>
 *         Generation Order:
 *         <p/>
 *         ApiListing ==> ApiDescriptions ==> Operations ==> Parameters
 *         ==> ResponseMessages
 *         <p/>
 *         Models are generated as they are detected and ModelReferences are added for each
 */
public class SpringMvcApiReader {
    private ApiSource apiSource;
    private ApiListing apiListing;
    private String resourcePath;
    private List<String> produces;
    private List<String> consumes;
    private HashMap<String, Model> models;

    private static final Option<String> DEFAULT_OPTION = Option.empty(); //<--comply with scala option to prevent nulls
    private static final String[] RESERVED_PACKAGES = {"java", "org.springframework"};
    private OverrideConverter overriderConverter;


    public SpringMvcApiReader(ApiSource aSource, OverrideConverter overrideConverter) {
        apiSource = aSource;
        apiListing = null;
        resourcePath = "";
        models = new HashMap<String, Model>();
        produces = new ArrayList<String>();
        consumes = new ArrayList<String>();
        this.overriderConverter = overrideConverter;
    }

    /**
     * @param basePath
     * @param c
     * @param methods            Run through annotations in approved controller class to generate ApiListing
     *                           This method is called from the document source class and calls the generating methods
     * @param overriderConverter
     * @param swaggerConfig
     */
    public ApiListing read(SpringResource resource, SwaggerConfig swaggerConfig) {
        List<Method> methods = resource.getMethods();
        List<String> protocols = new ArrayList<String>();
        List<ApiDescription> apiDescriptions = new ArrayList<ApiDescription>();
        List<Authorization> authorizations = Collections.emptyList();//TODO
        String newBasePath = apiSource.getBasePath();
        String description = null;
        int position = 0;

        // Add the description from the controller api
        Class<?> controller = resource.getControllerClass();
        if (controller != null && controller.isAnnotationPresent(Api.class)) {
            Api api = controller.getAnnotation(Api.class);
            description = api.description();
            position = api.position();
        }

        resourcePath = resource.getControllerMapping();
//        newBasePath = generateBasePath(apiSource.getBasePath(), resourcePath);

        Map<String, List<Method>> apiMethodMap = new HashMap<String, List<Method>>();
        for (Method m : methods) {
            if (m.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
                String path = "";
                if (requestMapping.value() != null && requestMapping.value().length != 0) {
                    path = generateFullPath(requestMapping.value()[0]);
                } else {
                    path = resourcePath;
                }
                if(apiMethodMap.containsKey(path)) {
                    apiMethodMap.get(path).add(m);
                } else {
                    List<Method> ms = new ArrayList<Method>();
                    ms.add(m);
                    apiMethodMap.put(path, ms);
                }
            }
        }
        for (String p : apiMethodMap.keySet()) {
            List<Operation> operations = new ArrayList<Operation>();
            for(Method m : apiMethodMap.get(p)){
                operations.add(generateOperation(m));
            }
            //reorder operations
            Collections.sort(operations, new Comparator<Operation>() {
                @Override
                public int compare(Operation o1, Operation o2) {
                    return o1.position() - o2.position();
                }
            });

            apiDescriptions.add(new ApiDescription(p, DEFAULT_OPTION,
                    scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(operations.iterator())), false));


        }

        apiListing = new ApiListing(swaggerConfig.apiVersion(), swaggerConfig.getSwaggerVersion(), newBasePath, resourcePath,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(produces.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(consumes.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(protocols.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizations.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiDescriptions.iterator())),
                generateModels(models), Option.apply(description), position);
        return apiListing;
    }

    //--------Swagger Resource Generators--------//


    private String generateBasePath(String bPath, String rPath) {
        String domain = "";

        //check for two character domain at beginning of resourcePath
        if (rPath.charAt(2) == '/') {
            domain = rPath.substring(0, 2);
            this.resourcePath = rPath.substring(2);
        } else if (rPath.charAt(3) == '/') {
            domain = rPath.substring(1, 3);
            this.resourcePath = rPath.substring(3);
        }

        //check for first & trailing backslash
        if (bPath.lastIndexOf('/') != (bPath.length() - 1) && StringUtils.isNotEmpty(domain)) {
            bPath = bPath + '/';
        }

        //TODO this should be done elsewhere
        if (this.resourcePath.charAt(0) != '/') {
            this.resourcePath = '/' + this.resourcePath;
        }

        return bPath + domain;
    }

    private String generateFullPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            return this.resourcePath + (path.startsWith("/") ? path : '/' + path);
        } else {
            return this.resourcePath;
        }
    }

    /**
     * Generates operations for the ApiDescription
     *
     * @param Method m
     * @return Operation
     */
    private Operation generateOperation(Method m) {
        Class<?> clazz;
        ApiOperation apiOperation;
        RequestMapping requestMapping;
        ResponseBody responseBody;
        String responseBodyName = "";
        String method = null;
        String description = null;
        String notes = null;
        List<String> opProduces = new ArrayList<String>();
        List<String> opConsumes = new ArrayList<String>();
        List<Parameter> parameters = new ArrayList<Parameter>();
        List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();

        apiOperation = m.getAnnotation(ApiOperation.class);
        requestMapping = m.getAnnotation(RequestMapping.class);
        responseBody = m.getAnnotation(ResponseBody.class);


        if (m.getReturnType().equals(ResponseEntity.class)) {
            clazz = getGenericSubtype(m.getReturnType(), m.getGenericReturnType());
        } else {
            clazz = m.getReturnType();
        }
        Class<?> containerClz = clazz;
        clazz = getGenericSubtype(m.getReturnType(), m.getGenericReturnType());

        if (requestMapping.produces() != null) {
            opProduces = Arrays.asList(requestMapping.produces());
            for (String str : opProduces) {
                if (!produces.contains(str)) {
                    produces.add(str);
                }
            }
        }
        if (requestMapping.consumes() != null) {
            opConsumes = Arrays.asList(requestMapping.consumes());
            for (String str : opConsumes) {
                if (!consumes.contains(str)) {
                    consumes.add(str);
                }
            }
        }

        if (apiOperation != null) {
            description = apiOperation.value();
            notes = apiOperation.notes();
        }

        responseMessages = generateResponseMessages(m);

        if (responseBody != null) {
            if (!containerClz.equals(clazz)) {
                responseBodyName = containerClz.getSimpleName() + "[" + clazz.getSimpleName() + "]";
            } else {
                responseBodyName = (clazz.getSimpleName());
            }
            addToModels(clazz);
        }
        if (requestMapping.method() != null && requestMapping.method().length != 0) {
            method = requestMapping.method()[0].toString();
        }
        if (m.getParameterTypes() != null) {
            parameters = generateParameters(m);
        }

        return new Operation(method,
                description, notes, responseBodyName, m.getName(), apiOperation.position(),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opProduces.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opConsumes.iterator())),
                null, null,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(parameters.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(responseMessages.iterator())),
                DEFAULT_OPTION);
    }

    /**
     * Generates parameters for each Operation
     *
     * @param Method m
     * @return List<Parameter>
     */
    private List<Parameter> generateParameters(Method m) {
        Annotation[][] annotations = m.getParameterAnnotations();
        List<Parameter> params = new ArrayList<Parameter>();
        for (int i = 0; i < annotations.length; i++) { //loops through parameters
            AllowableValues allowed = null;
            String dataType = "";
            String type = "";
            String name = "";
            boolean required = true;
            Annotation[] anns = annotations[i];
            Class<?> clazz = m.getParameterTypes()[i];
            String description = "";
            List<String> allowableValuesList;

            for (int x = 0; x < anns.length; x++) { //loops through annotations for each parameter
                if (anns[x].annotationType().equals(PathVariable.class)) {
                    PathVariable pathVariable = (PathVariable) anns[x];
                    name = pathVariable.value();
                    type = ApiValues.TYPE_PATH();
                } else if (anns[x].annotationType().equals(RequestBody.class)) {
                    RequestBody requestBody = (RequestBody) anns[x];
                    type = ApiValues.TYPE_BODY();
                    required = requestBody.required();
                } else if (anns[x].annotationType().equals(RequestParam.class)) {
                    RequestParam requestParam = (RequestParam) anns[x];
                    name = requestParam.value();
                    type = ApiValues.TYPE_QUERY();
                    required = requestParam.required();
                } else if (anns[x].annotationType().equals(RequestHeader.class)) {
                    RequestHeader requestHeader = (RequestHeader) anns[x];
                    name = requestHeader.value();
                    type = ApiValues.TYPE_HEADER();
                    required = requestHeader.required();
                } else if (anns[x].annotationType().equals(ApiParam.class)) {
                    try {
                        ApiParam apiParam = (ApiParam) anns[x];
                        if (apiParam.value() != null)
                            description = apiParam.value();
                        if (apiParam.allowableValues() != null) {
                            if (apiParam.allowableValues().startsWith("range")) {
                                String range = apiParam.allowableValues().substring("range".length());
                                String min, max;
                                Pattern pattern = Pattern.compile("\\[(.+),(.+)\\]");
                                Matcher matcher = pattern.matcher(range);
                                if(matcher.matches()){
                                    min = matcher.group(1);
                                    max = matcher.group(2);
                                    allowed = new AllowableRangeValues(min, max);
                                }
                            }else {
                                String allowableValues = CharMatcher.anyOf("[] ").removeFrom(apiParam.allowableValues());
                                if (!(allowableValues.equals(""))) {
                                    allowableValuesList = Arrays.asList(allowableValues.split(","));
                                    allowed = new AllowableListValues(
                                            scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(allowableValuesList.iterator())), "LIST");
                                }
                            }


                        }
                    } catch (Exception e) {
                    }
                }
            }

            if (clazz.isArray()) {
                String clazzName = CharMatcher.anyOf("[]").removeFrom(clazz.getSimpleName());
                clazzName = generateTypeString(clazzName);
                dataType = "Array[" + clazzName + "]";
                addToModels(clazz);
            } else {
                dataType = generateTypeString(clazz.getSimpleName());
                addToModels(clazz);
            }

            params.add(new Parameter(name, Option.apply(description), DEFAULT_OPTION, required, false, dataType,
                    allowed, type, DEFAULT_OPTION));
        }
        return params;
    }


    /**
     * Generates response messages for each Operation
     *
     * @param Method m
     * @return List<ResponseMessage>
     */
    private List<ResponseMessage> generateResponseMessages(Method m) {
        List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
        ApiResponses apiresponses = m.getAnnotation(ApiResponses.class);
        if (apiresponses != null) {
            for (ApiResponse apiResponse : apiresponses.value()) {
                if (apiResponse.response() == null || apiResponse.response().equals(java.lang.Void.class)) {
                    responseMessages.add(new ResponseMessage(apiResponse.code(), apiResponse.message(), Option.<String>empty()));
                } else {
                    addToModels(apiResponse.response());
                    responseMessages.add(new ResponseMessage(apiResponse.code(), apiResponse.message(), Option.apply(apiResponse.response().getSimpleName())));
                }
            }
        } else {
            ResponseStatus responseStatus = m.getAnnotation(ResponseStatus.class);

            responseMessages.add(new ResponseMessage(responseStatus.value().value(), responseStatus.reason(), DEFAULT_OPTION));
        }
        return responseMessages;
    }

    /**
     * Generates a Model object for a Java class. Takes properties from either fields or methods.
     * Recursion occurs if a ModelProperty needs to be modeled
     *
     * @param Class<?> clazz
     * @return Model
     */
    private Model generateModel(Class<?> clazz) {
        ApiModel apiModel;
        ModelRef modelRef = null;
        String modelDescription = "";
        LinkedHashMap<String, ModelProperty> modelProps = new LinkedHashMap<String, ModelProperty>();
        List<String> subTypes = new ArrayList<String>();

        if (clazz.isAnnotationPresent(ApiModel.class)) {
            apiModel = clazz.getAnnotation(ApiModel.class);
            if (apiModel.description() != null) {
                modelDescription = apiModel.description();
            }
        }

        //<--Model properties from fields-->
        int x = 0;
        for (Field field : clazz.getDeclaredFields()) {
            //Only use fields if they are annotated - otherwise use methods
            XmlElement xmlElement;
            ApiModelProperty amp;
            Class<?> c;
            String name;
            boolean required = false;
            String description = "";
            if (!(field.getType().equals(clazz))) {
                //if the types are the same, model will already be generated
                modelRef = generateModelRef(clazz, field); //recursive IFF there is a generic sub-type to be modeled
            }
            if (field.isAnnotationPresent(XmlElement.class) ||
                    field.isAnnotationPresent(ApiModelProperty.class)) {
                if (field.getAnnotation(XmlElement.class) != null) {
                    xmlElement = field.getAnnotation(XmlElement.class);
                    required = xmlElement.required();
                }
                if (field.getAnnotation(ApiModelProperty.class) != null) {
                    amp = field.getAnnotation(ApiModelProperty.class);
                    if (required != true) { //if required has already been changed to true, it was noted in XmlElement
                        required = amp.required();
                    }
                    description = amp.value();
                }

                if (!(field.getType().equals(clazz))) {
                    c = field.getType();
                    name = field.getName();
                    addToModels(c);
                    subTypes.add(c.getSimpleName());
                    modelProps.put(name, generateModelProperty(c, x, required, modelRef, description));
                    x++;
                }
            }
        }

        //<--Model properties from methods-->
        int i = 0;
        for (Method m : clazz.getMethods()) {
            boolean required = false;
            String description = "";
            ApiModelProperty amp;
            //look for required field in XmlElement annotation

            if (!(m.getReturnType().equals(clazz))) {
                modelRef = generateModelRef(clazz, m); //recursive IFF there is a generic sub-type to be modeled
            }

            if (m.isAnnotationPresent(XmlElement.class)) {
                XmlElement xmlElement = m.getAnnotation(XmlElement.class);
                required = xmlElement.required();
            } else if (m.isAnnotationPresent(JsonIgnore.class)
                    || m.isAnnotationPresent(XmlTransient.class)) {
                continue; //ignored fields
            }

            if (m.getAnnotation(ApiModelProperty.class) != null) {
                amp = m.getAnnotation(ApiModelProperty.class);
                required = amp.required();
                description = amp.value();
            }

            //get model properties from methods
            if ((m.getName().startsWith("get") || m.getName().startsWith("is"))
                    && !(m.getName().equals("getClass"))) {
                Class<?> c = m.getReturnType();
                String name = "";
                try {
                    if (m.getName().startsWith("get")) {
                        name = m.getName().substring(3);
                    } else {
                        name = m.getName().substring(2);
                    }
                    String firstLetter = name.substring(0, 1).toLowerCase(); //convert to camel case
                    name = firstLetter + name.substring(1);
                } catch (Exception e) {
                }
                if (!(m.getReturnType().equals(clazz))) {
                    addToModels(c); //recursive
                }
                if (!modelProps.contains(name)) {
                    modelProps.put(name, generateModelProperty(c, i, required, modelRef, description));
                }

            }
            i++;
        }

        return new Model(clazz.getSimpleName(), clazz.getSimpleName(), clazz.getCanonicalName(), modelProps,
                Option.apply(modelDescription), DEFAULT_OPTION, DEFAULT_OPTION,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(subTypes.iterator())));
    }

    /**
     * Generates a ModelProperty for given model class. Supports String enumerations only.
     *
     * @param Class<?> clazz
     * @param int      position
     * @param boolean  required
     * @param ModelRef modelRef
     * @param String   description
     * @return ModelProperty
     */
    private ModelProperty generateModelProperty(Class<?> clazz, int position, boolean required, ModelRef modelRef,
                                                String description) {
        AllowableListValues allowed = null;
        String name = clazz.getSimpleName();

        if (!(isModel(clazz))) {
            name = name.toLowerCase();
        }
        //check for enumerated values - currently strings only
        //TODO: support ranges
        if (clazz.isEnum()) {
            List<String> enums = new ArrayList<String>();
            for (Object obj : clazz.getEnumConstants()) {
                enums.add(obj.toString());
            }
            allowed = new AllowableListValues(
                    scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(enums.iterator())), "LIST");
        }

        return new ModelProperty(name,
                clazz.getCanonicalName(), position, required, Option.apply(description), allowed,
                Option.apply(modelRef));
    }

    /**
     * Generates a model reference based on a method
     *
     * @param Class<?> clazz
     * @param Method   m
     * @return ModelRef
     */
    private ModelRef generateModelRef(Class<?> clazz, Method m) {
        ModelRef modelRef = null; //can be null
        if (Collection.class.isAssignableFrom(m.getReturnType()) || m.getReturnType().equals(ResponseEntity.class)
                || m.getReturnType().equals(JAXBElement.class)) {
            Class<?> c = getGenericSubtype(m.getReturnType(), m.getGenericReturnType());
            if (isModel(c) && !(c.equals(clazz))) {
                addToModels(c);
                modelRef = new ModelRef(c.getSimpleName(), Option.apply(c.getSimpleName()),
                        Option.apply(c.getSimpleName()));
            } else {
                modelRef = new ModelRef(c.getSimpleName().toLowerCase(), DEFAULT_OPTION, DEFAULT_OPTION);
            }
        }
        return modelRef;
    }

    /**
     * Generates a model reference based on a field
     *
     * @param Class<?> clazz
     * @param Field    f
     * @return ModelRef
     */
    private ModelRef generateModelRef(Class<?> clazz, Field f) {
        ModelRef modelRef = null;
        if (Collection.class.isAssignableFrom(f.getType()) || f.getType().equals(ResponseEntity.class)
                || f.getType().equals(JAXBElement.class)) {
            Class<?> c = getGenericSubtype(f.getType(), f.getGenericType());
            if (isModel(c) && !(c.equals(clazz))) {
                addToModels(c);
                modelRef = new ModelRef(c.getSimpleName(), Option.apply(c.getSimpleName()),
                        Option.apply(c.getSimpleName()));
            } else {
                modelRef = new ModelRef(c.getSimpleName().toLowerCase(), DEFAULT_OPTION, DEFAULT_OPTION);
            }
        }
        return modelRef;
    }

    //-------------Helper Methods------------//

    private Class<?> getGenericSubtype(Class<?> clazz, Type t) {
        if (!(clazz.getName().equals("void") || t.toString().equals("void"))) {
            try {
                ParameterizedType paramType = (ParameterizedType) t;
                Type[] argTypes = paramType.getActualTypeArguments();
                if (argTypes.length > 0) {
                    Class<?> c = (Class<?>) argTypes[0];
                    return c;
                }
            } catch (ClassCastException e) {
                //FIXME: find out why this happens to only certain types
            }
        }
        return clazz;
    }

    private void addToModels(Class<?> clazz) {
        if (isModel(clazz) && !(models.containsKey(clazz.getSimpleName()))) {
            //put the key first in models to avoid stackoverflow
            models.put(clazz.getSimpleName(), new Model(null, null, null, null, null, null, null, null));
            scala.collection.mutable.HashMap<String, Option<Model>> overriderMap = this.overriderConverter.overrides();
            if (overriderMap.contains(clazz.getCanonicalName())) {
                Option<Option<Model>> m = overriderMap.get(clazz.getCanonicalName());
                models.put(clazz.getSimpleName(), m.get().get());
            } else {
                models.put(clazz.getSimpleName(), generateModel(clazz));
            }
        }
    }

    private boolean isModel(Class<?> clazz) {
        try {
            for (String str : RESERVED_PACKAGES) {
                if (clazz.getPackage().getName().contains(str)) {
                    return false;
                }
            }
            if (SwaggerSpec.baseTypes().contains(clazz.getSimpleName().toLowerCase())) {
                return false;
            } else {
                return true;
            }
        } catch (NullPointerException e) { //null pointer for package names - wouldn't model something without a package. skip
            return false;
        }
    }

    private String generateTypeString(String clazzName) {
        String typeString = clazzName;
        if (SwaggerSpec.baseTypes().contains(clazzName.toLowerCase())) {
            typeString = clazzName.toLowerCase();
        }
        return typeString;
    }

    private Option<scala.collection.immutable.Map<String, Model>> generateModels(HashMap<String, Model> javaModelMap) {
        Option<scala.collection.immutable.Map<String, Model>> models = Option.apply(Utils.toScalaImmutableMap(javaModelMap));
        return models;
    }


}
