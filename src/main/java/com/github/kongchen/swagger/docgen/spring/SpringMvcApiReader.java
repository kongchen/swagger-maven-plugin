package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.util.LogAdapter;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.AuthorizationScope;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.ModelProperty;
import com.wordnik.swagger.model.ModelRef;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
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
    private final ParameterGenerator parameterGenerator;
    private ApiSource apiSource;
    private ApiListing apiListing;
    private String resourcePath;
    private List<String> produces;
    private List<String> consumes;
    private HashMap<String, Model> models;

    private static final Option<String> DEFAULT_OPTION = Option.empty(); //<--comply with scala option to prevent nulls
    private static final String[] RESERVED_PACKAGES = {"java", "org.springframework"};
    private final LogAdapter logger;
    private OverrideConverter overriderConverter;

    public SpringMvcApiReader(ApiSource aSource, LogAdapter logger, OverrideConverter overrideConverter) {
        apiSource = aSource;
        this.logger = logger;
        apiListing = null;
        resourcePath = "";
        models = new HashMap<String, Model>();
        produces = new ArrayList<String>();
        consumes = new ArrayList<String>();
        this.overriderConverter = overrideConverter;
        this.parameterGenerator = new ParameterGenerator(logger);
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
        List<Authorization> authorizations = new ArrayList<Authorization>();
        String newBasePath = apiSource.getBasePath();
        String description = null;
        int position = 0;

        // Add the description from the controller api
        Class<?> controller = resource.getControllerClass();
        if (controller != null && controller.isAnnotationPresent(Api.class)) {
            Api api = controller.getAnnotation(Api.class);
            description = api.description();
            position = api.position();
            if (api.authorizations() != null && api.authorizations().length > 0) {
                addAuthorization(authorizations, api.authorizations());
            }
        }

        resourcePath = resource.getControllerMapping();

        Map<String, List<Method>> apiMethodMap = new HashMap<String, List<Method>>();
        for (Method m : methods) {
            if (m.isAnnotationPresent(RequestMapping.class)) {
                String path = getMethodPath(m.getAnnotation(RequestMapping.class));
                if (apiMethodMap.containsKey(path)) {
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
            for (Method m : apiMethodMap.get(p)) {
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

    private String getMethodPath(RequestMapping requestMapping) {
        if (requestMapping.value() != null && requestMapping.value().length != 0) {
            return generateFullPath(requestMapping.value()[0]);
        } else {
            return resourcePath;
        }
    }

    private void addAuthorization(List<Authorization> authorizations, com.wordnik.swagger.annotations.Authorization[] annotations) {
        for (com.wordnik.swagger.annotations.Authorization authorization : annotations) {
            List<AuthorizationScope> scopes = new ArrayList<AuthorizationScope>();
            for (com.wordnik.swagger.annotations.AuthorizationScope scope : authorization.scopes()) {
                scopes.add(new AuthorizationScope(scope.scope(), scope.description()));
            }
            authorizations.add(new Authorization(authorization.value(), scopes.toArray(new AuthorizationScope[scopes.size()])));
        }
    }

    //--------Swagger Resource Generators--------//

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
        String method = null;
        String description = null;
        String notes = null;

        List<Authorization> authorizations = new ArrayList<Authorization>();

        RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
        ResponseBody responseBody = m.getAnnotation(ResponseBody.class);

        Class<?> containerClz = getReturnedType(m);
        Class<?> clazz = getGenericSubtype(m.getReturnType(), m.getGenericReturnType());

        List<String> opProduces = asListOrEmptyIfNull(requestMapping.produces());
        addWithoutDuplicates(produces, opProduces);
        List<String> opConsumes = asListOrEmptyIfNull(requestMapping.consumes());
        addWithoutDuplicates(consumes, opConsumes);


        ApiOperation apiOperation = m.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            description = apiOperation.value();
            notes = apiOperation.notes();
            if (apiOperation.authorizations() != null && apiOperation.authorizations().length > 0) {
                addAuthorization(authorizations, apiOperation.authorizations());
            }
        }

        List<ResponseMessage> responseMessages = generateResponseMessages(m);

        String responseBodyName = "";
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

        List<Parameter> parameters = new ArrayList<Parameter>();
        if (m.getParameterTypes() != null) {
            parameters = generateParameters(m);
        }

        return new Operation(method,
                description, notes, responseBodyName, m.getName(), apiOperation.position(),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opProduces.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opConsumes.iterator())),
                null,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizations.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(parameters.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(responseMessages.iterator())),
                DEFAULT_OPTION);
    }

    private Class<?> getReturnedType(Method m) {
        if (m.getReturnType().equals(ResponseEntity.class)) {
            return getGenericSubtype(m.getReturnType(), m.getGenericReturnType());
        } else {
            return m.getReturnType();
        }
    }

    private static <T> List<T> asListOrEmptyIfNull(T[] array) {
        if (array == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(array);
        }
    }

    private static <T> void addWithoutDuplicates(Collection<T> targetCollection, Collection<T> elements) {
        for (T str : elements) {
            if (!targetCollection.contains(str)) {
                targetCollection.add(str);
            }
        }
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
            Class<?> parameterClass = m.getParameterTypes()[i];
            addToModels(parameterClass);
            Parameter parameter = parameterGenerator.generateParameter(new ParameterMetadata(parameterClass, annotations[i]));
            params.add(parameter);
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

        for (Field field : sortFields(clazz)) {
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
                    if (!required) { //if required has already been changed to true, it was noted in XmlElement
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
        for (Method m : sortMethodsGettersFirst(clazz)) {
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
            if (isGetterMethod(m)) {
                String name = getPropertyNameFromMethodName(m);
                if (!(m.getReturnType().equals(clazz))) {
                    addToModels(m.getReturnType()); //recursive
                }
                if (!modelProps.contains(name)) {
                    modelProps.put(name, generateModelProperty(m.getReturnType(), i, required, modelRef, description));
                }
            }
            i++;
        }

        return new Model(clazz.getSimpleName(), clazz.getSimpleName(), clazz.getCanonicalName(), modelProps,
                Option.apply(modelDescription), DEFAULT_OPTION, DEFAULT_OPTION,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(subTypes.iterator())));
    }

    private static Field[] sortFields(Class<?> clazz) {
        Field[] sortedFields = clazz.getDeclaredFields();
        Arrays.sort(sortedFields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return sortedFields;
    }

    private static Method[] sortMethodsGettersFirst(Class<?> clazz) {
        Method[] ms = clazz.getMethods();
        Arrays.sort(ms, new Comparator<Method>() {
            @Override
            public int compare(Method m1, Method m2) {
                boolean m1IsGetter = isGetterMethod(m1);
                boolean m2IsGetter = isGetterMethod(m2);
                if (m1IsGetter) {
                    if (m2IsGetter) {
                        return getPropertyNameFromMethodName(m1).compareTo(getPropertyNameFromMethodName(m2));
                    } else {
                        return 1;
                    }
                } else {
                    if (!m2IsGetter) {
                        return m1.getName().compareTo(m2.getName());
                    } else {
                        return -1;
                    }
                }
            }
        });
        return ms;
    }

    private static String getPropertyNameFromMethodName(Method method) {
        String stripPrefix;
        String methodName = method.getName();
        if (methodName.startsWith("get") && methodName.length() > 3) {
            stripPrefix = methodName.substring(3);
        } else if (methodName.startsWith("is") && methodName.length() > 2) {
            stripPrefix = methodName.substring(2);
        } else {
            stripPrefix = methodName;
        }
        return lowerCamelCase(stripPrefix);
    }

    private static String lowerCamelCase(String name) {
        if (name.isEmpty()) {
            return name;
        }
        String lowerFirstLetter = name.substring(0, 1).toLowerCase(); //convert to camel case
        if (name.length() > 1) {
            return lowerFirstLetter + name.substring(1);
        } else {
            return lowerFirstLetter;
        }
    }

    private static boolean isGetterMethod(Method m) {
        String name = m.getName();
        return (name.startsWith("get") || name.startsWith("is")) && !(name.equals("getClass"));
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
    private static ModelProperty generateModelProperty(Class<?> clazz, int position, boolean required, ModelRef modelRef,
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

    private static Class<?> getGenericSubtype(Class<?> clazz, Type t) {
        if (!(clazz.getName().equals("void") || t.toString().equals("void"))) {
            try {
                ParameterizedType paramType = (ParameterizedType) t;
                Type[] argTypes = paramType.getActualTypeArguments();
                if (argTypes.length > 0) {
                    return (Class<?>) argTypes[0];
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

    private static boolean isModel(Class<?> clazz) {
        try {
            for (String str : RESERVED_PACKAGES) {
                if (clazz.getPackage().getName().contains(str)) {
                    return false;
                }
            }
            return !SwaggerSpec.baseTypes().contains(clazz.getSimpleName().toLowerCase());
        } catch (NullPointerException e) { //null pointer for package names - wouldn't model something without a package. skip
            return false;
        }
    }

    private Option<scala.collection.immutable.Map<String, Model>> generateModels(HashMap<String, Model> javaModelMap) {
        return Option.apply(Utils.toScalaImmutableMap(javaModelMap));
    }


}
