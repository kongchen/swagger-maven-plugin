package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import com.wordnik.swagger.annotations.AuthorizationScope;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.jaxrs.ParameterProcessor;
import com.wordnik.swagger.jaxrs.ext.SwaggerExtension;
import com.wordnik.swagger.jaxrs.ext.SwaggerExtensions;
import com.wordnik.swagger.jaxrs.utils.ParameterUtils;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Response;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.SecurityDefinition;
import com.wordnik.swagger.models.SecurityRequirement;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.Tag;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.MapProperty;
import com.wordnik.swagger.models.properties.Property;
import com.wordnik.swagger.models.properties.RefProperty;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

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
    private LogAdapter LOG;
    private final Swagger swagger;
    private ApiSource apiSource;
    private Path apiListing;
    private String resourcePath;




    public SpringMvcApiReader(ApiSource aSource, Swagger swagger, LogAdapter LOG) {
        this.LOG = LOG;
        this.swagger = swagger;
        apiSource = aSource;
        apiListing = null;
        resourcePath = "";

        List<SwaggerExtension> swaggerExtensions = new ArrayList<SwaggerExtension>();
        swaggerExtensions.add(new SpringSwaggerExtension());
        SwaggerExtensions.setExtensions(swaggerExtensions);
//        this.overriderConverter = overrideConverter;
    }

    private boolean canReadApi(boolean readHidden, Api api) {
        return (api != null && readHidden) || (api != null && !api.hidden());
    }

    protected Set<String> extractTags(Api api) {
        Set<String> output = new LinkedHashSet<String>();

        boolean hasExplicitTags = false;
        for (String tag : api.tags()) {
            if (!"".equals(tag)) {
                hasExplicitTags = true;
                output.add(tag);
            }
        }
        if (!hasExplicitTags) {
            // derive tag from api path + description
            String tagString = api.value().replace("/", "");
            if (!"".equals(tagString))
                output.add(tagString);
        }
        return output;
    }

    private Map<String, Tag> updateTagsForApi(Map<String, Tag> parentTags, Api api) {
        // the value will be used as a tag for 2.0 UNLESS a Tags annotation is present
        Set<String> tagStrings = extractTags(api);
        Map<String, Tag> tags = new HashMap<String, Tag>();
        for (String tagString : tagStrings) {
            Tag tag = new Tag().name(tagString);
            tags.put(tagString, tag);
        }
        if (parentTags != null)
            tags.putAll(parentTags);
        for (String tagName : tags.keySet()) {
            swagger.tag(tags.get(tagName));
        }
        return tags;
    }

    private List<SecurityRequirement> getSecurityRequirements(Api api) {
        int position = api.position();
        String produces = api.produces();
        String consumes = api.consumes();
        String schems = api.protocols();
        Authorization[] authorizations = api.authorizations();

        List<SecurityRequirement> securities = new ArrayList<SecurityRequirement>();
        for (Authorization auth : authorizations) {
            if (auth.value() != null && !"".equals(auth.value())) {
                SecurityRequirement security = new SecurityRequirement();
                security.setName(auth.value());
                AuthorizationScope[] scopes = auth.scopes();
                for (AuthorizationScope scope : scopes) {
                    if (scope.scope() != null && !"".equals(scope.scope())) {
                        security.addScope(scope.scope());
                    }
                }
                securities.add(security);
            }
        }
        return securities;
    }

    private String parseOperationPath(String operationPath, Map<String, String> regexMap) {
        String[] pps = operationPath.split("/");
        String[] pathParts = new String[pps.length];


        for (int i = 0; i < pps.length; i++) {
            String p = pps[i];
            if (p.startsWith("{")) {
                int pos = p.indexOf(":");
                if (pos > 0) {
                    String left = p.substring(1, pos);
                    String right = p.substring(pos + 1, p.length() - 1);
                    pathParts[i] = "{" + left + "}";
                    regexMap.put(left, right);
                } else
                    pathParts[i] = p;
            } else pathParts[i] = p;
        }
        StringBuilder pathBuilder = new StringBuilder();
        for (String p : pathParts) {
            if (!p.isEmpty())
                pathBuilder.append("/").append(p);
        }
        operationPath = pathBuilder.toString();
        return operationPath;
    }

    public Map<String, Property> parseResponseHeaders(com.wordnik.swagger.annotations.ResponseHeader[] headers) {
        Map<String, Property> responseHeaders = null;
        if (headers != null && headers.length > 0) {
            for (com.wordnik.swagger.annotations.ResponseHeader header : headers) {
                String name = header.name();
                if (!"".equals(name)) {
                    if (responseHeaders == null)
                        responseHeaders = new HashMap<String, Property>();
                    String description = header.description();
                    Class<?> cls = header.response();
                    String container = header.responseContainer();

                    if (!cls.equals(Void.class) && !"void".equals(cls.toString())) {
                        Property responseProperty = null;
                        Property property = ModelConverters.getInstance().readAsProperty(cls);
                        if (property != null) {
                            if ("list".equalsIgnoreCase(container))
                                responseProperty = new ArrayProperty(property);
                            else if ("map".equalsIgnoreCase(container))
                                responseProperty = new MapProperty(property);
                            else
                                responseProperty = property;
                            responseProperty.setDescription(description);
                            responseHeaders.put(name, responseProperty);
                        }
                    }
                }
            }
        }
        return responseHeaders;
    }


    boolean isPrimitive(Class<?> cls) {
        boolean out = false;

        Property property = ModelConverters.getInstance().readAsProperty(cls);
        if (property == null)
            out = false;
        else if ("integer".equals(property.getType()))
            out = true;
        else if ("string".equals(property.getType()))
            out = true;
        else if ("number".equals(property.getType()))
            out = true;
        else if ("boolean".equals(property.getType()))
            out = true;
        else if ("array".equals(property.getType()))
            out = true;
        else if ("file".equals(property.getType()))
            out = true;
        return out;
    }

    private void updateOperationParameters(List<Parameter> parentParameters, Map<String, String> regexMap, Operation operation) {
        if (parentParameters != null) {
            for (Parameter param : parentParameters) {
                operation.parameter(param);
            }
        }
        for (Parameter param : operation.getParameters()) {
            if (regexMap.get(param.getName()) != null) {
                String pattern = regexMap.get(param.getName());
                param.setPattern(pattern);
            }
        }
    }

    private void updateOperationProtocols(ApiOperation apiOperation, Operation operation) {
        String protocols = apiOperation.protocols();
        if (!"".equals(protocols)) {
            String[] parts = protocols.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!"".equals(trimmed))
                    operation.scheme(Scheme.forValue(trimmed));
            }
        }
    }

    public Swagger read(SpringResource resource) {
        List<Method> methods = resource.getMethods();
        List<String> protocols = new ArrayList<String>();
        List<Operation> operationList = new ArrayList<Operation>();
        String newBasePath = apiSource.getBasePath();
        String description = null;
        int position = 0;
        Map<String, Tag> tags = new HashMap<String, Tag>();

        List<SecurityRequirement> resourceSecurities = new ArrayList<SecurityRequirement>();

        // Add the description from the controller api
        Class<?> controller = resource.getControllerClass();
        RequestMapping apiPath = controller.getAnnotation(RequestMapping.class);


        if (controller != null && controller.isAnnotationPresent(Api.class)) {
            Api api = controller.getAnnotation(Api.class);
            if (!canReadApi(false, api)) {
                return null;
            }
            tags = updateTagsForApi(null, api);

            resourceSecurities = getSecurityRequirements(api);

            description = api.description();
            position = api.position();
        }

        resourcePath = resource.getControllerMapping();

        Map<String, List<Method>> apiMethodMap = new HashMap<String, List<Method>>();

        //collect api from method with @RequestMapping
        collectApisByRequestMapping(methods, apiMethodMap);

        for (String p : apiMethodMap.keySet()) {
            List<Operation> operations = new ArrayList<Operation>();

            for (Method m : apiMethodMap.get(p)) {

                RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
                ApiOperation apiOperation = m.getAnnotation(ApiOperation.class);

                String operationPath = p;
                //getPath(apiPath, requestMapping, "");
                String operationId;
                String httpMethod = null;

                if (operationPath != null && apiOperation != null) {


                    Map<String, String> regexMap = new HashMap<String, String>();
                    operationPath = parseOperationPath(operationPath, regexMap);

                    //http method
                    if (requestMapping.method() != null && requestMapping.method().length != 0) {
                        httpMethod = requestMapping.method()[0].toString();
                    }

                    Operation operation = parseMethod(m);

                    updateOperationParameters(new ArrayList<Parameter>(), regexMap, operation);

                    updateOperationProtocols(apiOperation, operation);

                    String[] apiConsumes = new String[0];
                    String[] apiProduces = new String[0];
                    RequestMapping rm = controller.getAnnotation(RequestMapping.class);

                    String[] pps = new String[0];
                    String[] pcs = new String[0];
                    if (rm != null) {
                        pcs = rm.consumes();
                        pps = rm.produces();
                    }

                    apiConsumes = updateOperationConsumes(m, pcs, apiConsumes, operation);
                    apiProduces = updateOperationProduces(m, pps, apiProduces, operation);

                    // can't continue without a valid http method
//                    httpMethod = httpMethod == null ? parentMethod : httpMethod;
                    ApiOperation op = m.getAnnotation(ApiOperation.class);
                    updateTagsForOperation(operation, op);
                    updateOperation(apiConsumes, apiProduces, tags, resourceSecurities, operation);
                    updatePath(operationPath, httpMethod, operation);
                }


            }

        }
        return swagger;
    }


    private void updatePath(String operationPath, String httpMethod, Operation operation) {
        if (httpMethod == null) {
            return;
        }
        Path path = swagger.getPath(operationPath);
        if (path == null) {
            path = new Path();
            swagger.path(operationPath, path);
        }
        path.set(httpMethod.toLowerCase(), operation);
    }

    private void updateOperation(String[] apiConsumes, String[] apiProduces, Map<String, Tag> tags, List<SecurityRequirement> securities, Operation operation) {
        if (operation == null) {
            return;
        }
        if (operation.getConsumes() == null) {
            for (String mediaType : apiConsumes) {
                operation.consumes(mediaType);
            }
        }
        if (operation.getProduces() == null) {
            for (String mediaType : apiProduces) {
                operation.produces(mediaType);
            }
        }

        if (operation.getTags() == null) {
            for (String tagString : tags.keySet()) {
                operation.tag(tagString);
            }
        }
        for (SecurityRequirement security : securities) {
            operation.security(security);
        }
    }

    private void updateTagsForOperation(Operation operation, ApiOperation op) {
        if (op != null) {
            boolean hasExplicitTag = false;
            for (String tag : op.tags()) {
                if (!"".equals(tag)) {
                    operation.tag(tag);
                    swagger.tag(new Tag().name(tag));
                }
            }
        }
    }

//    private void handleSubResource(String[] apiConsumes, String httpMethod, String[] apiProduces, Map<String, Tag> tags, Method method, String operationPath, Operation operation) {
//        if (isSubResource(method)) {
//            Type t = method.getGenericReturnType();
//            Class<?> responseClass = method.getReturnType();
//            Swagger subSwagger = read(responseClass, operationPath,httpMethod, true, apiConsumes, apiProduces, tags, operation.getParameters());
//        }
//    }

    private String[] updateOperationProduces(Method cls, String[] parentProduces, String[] apiProduces, Operation operation) {
        RequestMapping requestMapping = cls.getAnnotation(RequestMapping.class);
        if (requestMapping != null)
            apiProduces = requestMapping.produces();

        if (parentProduces != null) {
            Set<String> both = new HashSet<String>(Arrays.asList(apiProduces));
            both.addAll(new HashSet<String>(Arrays.asList(parentProduces)));
            if (operation.getProduces() != null)
                both.addAll(new HashSet<String>(operation.getProduces()));
            apiProduces = both.toArray(new String[both.size()]);
        }
        return apiProduces;
    }

    private String[] updateOperationConsumes(Method cls, String[] parentConsumes, String[] apiConsumes, Operation operation) {

        RequestMapping requestMapping = cls.getAnnotation(RequestMapping.class);
        if (requestMapping != null)
            apiConsumes = requestMapping.consumes();

        if (parentConsumes != null) {
            Set<String> both = new HashSet<String>(Arrays.asList(apiConsumes));
            both.addAll(new HashSet<String>(Arrays.asList(parentConsumes)));
            if (operation.getConsumes() != null)
                both.addAll(new HashSet<String>(operation.getConsumes()));
            apiConsumes = both.toArray(new String[both.size()]);
        }
        return apiConsumes;
    }

    private Operation parseMethod(Method m) {
        Operation operation = new Operation();
        RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
        Class<?> responseClass = null;
        List<String> produces = new ArrayList<String>();
        List<String> consumes = new ArrayList<String>();
        String responseContainer = null;
        String operationId = m.getName();
        Map<String, Property> defaultResponseHeaders;

        ApiOperation apiOperation = m.getAnnotation(ApiOperation.class);

        if (apiOperation.hidden())
            return null;
        if (!"".equals(apiOperation.nickname()))
            operationId = m.getName();

        defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders());

        operation.summary(apiOperation.value()).description(apiOperation.notes());

        if (apiOperation.response() != null && !Void.class.equals(apiOperation.response()))
            responseClass = apiOperation.response();
        if (!"".equals(apiOperation.responseContainer()))
            responseContainer = apiOperation.responseContainer();

        ///security
        if (apiOperation.authorizations() != null) {
            List<SecurityRequirement> securities = new ArrayList<SecurityRequirement>();
            for (Authorization auth : apiOperation.authorizations()) {
                if (auth.value() != null && !"".equals(auth.value())) {
                    SecurityRequirement security = new SecurityRequirement();
                    security.setName(auth.value());
                    AuthorizationScope[] scopes = auth.scopes();
                    for (AuthorizationScope scope : scopes) {
                        SecurityDefinition definition = new SecurityDefinition(auth.type());
                        if (scope.scope() != null && !"".equals(scope.scope())) {
                            security.addScope(scope.scope());
                            definition.scope(scope.scope(), scope.description());
                        }
                    }
                    securities.add(security);
                }
            }
            if (securities.size() > 0) {
                for (SecurityRequirement sec : securities)
                    operation.security(sec);
            }
        }

        if (responseClass == null) {
            Type t = m.getGenericReturnType();
            responseClass = m.getReturnType();
            if (responseClass.equals(ResponseEntity.class)) {
                responseClass = getGenericSubtype(m.getReturnType(), m.getGenericReturnType());
            }
            if (!responseClass.equals(Void.class) && !"void".equals(responseClass.toString()) && responseClass.getAnnotation(Api.class) == null) {
                LOG.info("reading model " + responseClass);
                Map<String, Model> models = ModelConverters.getInstance().readAll(t);
            }
        }
        if (responseClass != null
                && !responseClass.equals(Void.class)
                && !responseClass.equals(ResponseEntity.class)
                && responseClass.getAnnotation(Api.class) == null) {
            if (isPrimitive(responseClass)) {
                Property responseProperty = null;
                Property property = ModelConverters.getInstance().readAsProperty(responseClass);
                if (property != null) {
                    if ("list".equalsIgnoreCase(responseContainer))
                        responseProperty = new ArrayProperty(property);
                    else if ("map".equalsIgnoreCase(responseContainer))
                        responseProperty = new MapProperty(property);
                    else
                        responseProperty = property;
                    operation.response(200, new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                }
            } else if (!responseClass.equals(Void.class) && !"void".equals(responseClass.toString())) {
                Map<String, Model> models = ModelConverters.getInstance().read(responseClass);
                if (models.size() == 0) {
                    Property pp = ModelConverters.getInstance().readAsProperty(responseClass);
                    operation.response(200, new Response()
                            .description("successful operation")
                            .schema(pp)
                            .headers(defaultResponseHeaders));
                }
                for (String key : models.keySet()) {
                    Property responseProperty = null;

                    if ("list".equalsIgnoreCase(responseContainer))
                        responseProperty = new ArrayProperty(new RefProperty().asDefault(key));
                    else if ("map".equalsIgnoreCase(responseContainer))
                        responseProperty = new MapProperty(new RefProperty().asDefault(key));
                    else
                        responseProperty = new RefProperty().asDefault(key);
                    operation.response(200, new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                    swagger.model(key, models.get(key));
                }
                models = ModelConverters.getInstance().readAll(responseClass);
                for (String key : models.keySet()) {
                    swagger.model(key, models.get(key));
                }
            }
        }

        operation.operationId(operationId);

        if (requestMapping.produces() != null) {
            for (String str : Arrays.asList(requestMapping.produces())) {
                if (!produces.contains(str)) {
                    produces.add(str);
                }
            }
        }
        if (requestMapping.consumes() != null) {
            for (String str :  Arrays.asList(requestMapping.consumes())) {
                if (!consumes.contains(str)) {
                    consumes.add(str);
                }
            }
        }

        ApiResponses responseAnnotation = m.getAnnotation(ApiResponses.class);
        if (responseAnnotation != null) {
            for (ApiResponse apiResponse : responseAnnotation.value()) {
                Map<String, Property> responseHeaders = parseResponseHeaders(apiResponse.responseHeaders());

                Response response = new Response()
                        .description(apiResponse.message())
                        .headers(responseHeaders);

                if (apiResponse.code() == 0)
                    operation.defaultResponse(response);
                else
                    operation.response(apiResponse.code(), response);

                responseClass = apiResponse.response();
                if (responseClass != null && !responseClass.equals(Void.class)) {
                    Map<String, Model> models = ModelConverters.getInstance().read(responseClass);
                    for (String key : models.keySet()) {
                        response.schema(new RefProperty().asDefault(key));
                        swagger.model(key, models.get(key));
                    }
                    models = ModelConverters.getInstance().readAll(responseClass);
                    for (String key : models.keySet()) {
                        swagger.model(key, models.get(key));
                    }
                }
            }
        } else {
            ResponseStatus responseStatus = m.getAnnotation(ResponseStatus.class);
            if (responseStatus != null) {
                operation.response(responseStatus.value().value(), new Response().description(responseStatus.reason()));
            }
        }

        boolean isDeprecated = false;
        Deprecated annotation = m.getAnnotation(Deprecated.class);
        if (annotation != null)
            isDeprecated = true;

        boolean hidden = false;
        if (apiOperation != null)
            hidden = apiOperation.hidden();

        // process parameters
        Class[] parameterTypes = m.getParameterTypes();
        Type[] genericParameterTypes = m.getGenericParameterTypes();
        Annotation[][] paramAnnotations = m.getParameterAnnotations();
        // paramTypes = method.getParameterTypes
        // genericParamTypes = method.getGenericParameterTypes
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> cls = parameterTypes[i];
            Type type = genericParameterTypes[i];
            List<Parameter> parameters = getParameters(cls, type, paramAnnotations[i]);

            for (Parameter parameter : parameters) {
                operation.parameter(parameter);
            }
        }

        if (operation.getResponses() == null) {
            operation.defaultResponse(new Response().description("successful operation"));
        }

        return operation;

    }

    private List<Parameter> getParameters(Class<?> cls, Type type, Annotation[] annotations) {
        // look for path, query
        boolean isArray = ParameterUtils.isMethodArgumentAnArray(cls, type);
        Iterator<SwaggerExtension> chain = SwaggerExtensions.chain();
        List<Parameter> parameters = null;

        LOG.info("getParameters for " + cls);
        Set<Class<?>> classesToSkip = new HashSet<Class<?>>();
        if (chain.hasNext()) {
            SwaggerExtension extension = chain.next();
            LOG.info("trying extension " + extension);
            parameters = extension.extractParameters(annotations, cls, isArray, classesToSkip, chain);
        }

        if (parameters.size() > 0) {
            for (Parameter parameter : parameters) {
                ParameterProcessor.applyAnnotations(swagger, parameter, cls, annotations, isArray);
            }
        } else {
            LOG.info("no parameter found, looking at body params");
            if (classesToSkip.contains(cls) == false) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType ti = (ParameterizedType) type;
                    Type innerType = ti.getActualTypeArguments()[0];
                    if (innerType instanceof Class) {
                        Parameter param = ParameterProcessor.applyAnnotations(swagger, null, (Class) innerType, annotations, isArray);
                        if (param != null) {
                            parameters.add(param);
                        }
                    }
                } else {
                    Parameter param = ParameterProcessor.applyAnnotations(swagger, null, cls, annotations, isArray);
                    if (param != null) {
                        parameters.add(param);
                    }
                }
            }
        }
        return parameters;
    }


    private void collectApisByRequestMapping(List<Method> methods, Map<String, List<Method>> apiMethodMap) {
        for (Method m : methods) {
            if (m.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
                String path = "";
                if (requestMapping.value() != null && requestMapping.value().length != 0) {
                    path = generateFullPath(requestMapping.value()[0]);
                } else {
                    path = resourcePath;
                }
                if (apiMethodMap.containsKey(path)) {
                    apiMethodMap.get(path).add(m);
                } else {
                    List<Method> ms = new ArrayList<Method>();
                    ms.add(m);
                    apiMethodMap.put(path, ms);
                }
            }
        }
    }

    private SecurityDefinition getSecurityDefinition(Authorization[] annotations) {
        SecurityDefinition securityDefinition = new SecurityDefinition();
        for (Authorization authorization : annotations) {
            List<AuthorizationScope> scopes = new ArrayList<AuthorizationScope>();
            for (AuthorizationScope scope : authorization.scopes()) {

                securityDefinition.addScope(scope.scope(), scope.description());
            }

        }
        return securityDefinition;
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

    String getPath(RequestMapping classLevelPath, RequestMapping methodLevelPath, String parentPath) {
        if (classLevelPath == null && methodLevelPath == null)
            return null;
        StringBuilder b = new StringBuilder();
        if (parentPath != null && !"".equals(parentPath) && !"/".equals(parentPath)) {
            if (!parentPath.startsWith("/"))
                parentPath = "/" + parentPath;
            if (parentPath.endsWith("/"))
                parentPath = parentPath.substring(0, parentPath.length() - 1);

            b.append(parentPath);
        }
        if (classLevelPath != null) {
            b.append(classLevelPath.value());
        }
        if (methodLevelPath != null && !"/".equals(methodLevelPath.value())) {
            String methodPath = methodLevelPath.value()[0];
            if (!methodPath.startsWith("/") && !b.toString().endsWith("/")) {
                b.append("/");
            }
            if (methodPath.endsWith("/")) {
                methodPath = methodPath.substring(0, methodPath.length() - 1);
            }
            b.append(methodPath);
        }
        String output = b.toString();
        if (!output.startsWith("/"))
            output = "/" + output;
        if (output.endsWith("/") && output.length() > 1)
            return output.substring(0, output.length() - 1);
        else
            return output;
    }

    private Field[] sortFields(Class<?> clazz) {
        Field[] sortedFields = clazz.getDeclaredFields();
        Arrays.sort(sortedFields, new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return sortedFields;
    }

    private Method[] sortMethods(Class<?> clazz) {
        Method[] ms = clazz.getMethods();
        Arrays.sort(ms, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                String m1 = getModelNameFromGetterMethodName(o1);
                String m2 = getModelNameFromGetterMethodName(o2);
                if (m1 != null) {
                    if (m2 != null) {
                        return m1.compareTo(m2);
                    } else {
                        return 1;
                    }
                } else {
                    if (m2 == null) {
                        return o1.getName().compareTo(o2.getName());
                    } else {
                        return -1;
                    }
                }
            }
        });
        return ms;
    }

    private String getModelNameFromGetterMethodName(Method m) {
        String name = null;
        if ((m.getName().startsWith("get") || m.getName().startsWith("is"))
                && !(m.getName().equals("getClass"))) {
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
        }
        return name;
    }


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

        Map<String, Model> models = ModelConverters.getInstance().read(clazz);
        for (String key : models.keySet()) {
            swagger.model(key, models.get(key));
        }
        models = ModelConverters.getInstance().readAll(clazz);
        for (String key : models.keySet()) {
            swagger.model(key, models.get(key));
        }

    }


    private String generateTypeString(String clazzName) {
        String typeString = clazzName;

        try {
            if (isPrimitive(Class.forName(clazzName))) {
                typeString = clazzName.toLowerCase();
            }
        } catch (ClassNotFoundException e) {

        }
        return typeString;
    }

}
