package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import com.github.kongchen.swagger.docgen.spring.SpringSwaggerExtension;
import com.github.kongchen.swagger.docgen.util.SprintUtils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import com.wordnik.swagger.annotations.AuthorizationScope;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.jaxrs.ext.SwaggerExtension;
import com.wordnik.swagger.jaxrs.ext.SwaggerExtensions;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Response;
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
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpringMvcApiReader extends AbstractReader implements ClassSwaggerReader {
    private String resourcePath;


    public SpringMvcApiReader(Swagger swagger, LogAdapter log) {
        super(swagger, log);


        List<SwaggerExtension> swaggerExtensions = new ArrayList<SwaggerExtension>();
        swaggerExtensions.add(new SpringSwaggerExtension());
        SwaggerExtensions.setExtensions(swaggerExtensions);
    }

    @Override
    public Swagger read(Set<Class<?>> classes) throws GenerateException {
        //relate all methods to one base request mapping if multiple controllers exist for that mapping
        //get all methods from each controller & find their request mapping
        //create map - resource string (after first slash) as key, new SpringResource as value
        Map<String, SpringResource> resourceMap = generateResourceMap(classes);
        for (String str : resourceMap.keySet()) {
            SpringResource resource = resourceMap.get(str);
            read(resource);
        }

        return swagger;
    }


    public Swagger read(SpringResource resource) {
        if (swagger == null) {
            swagger = new Swagger();
        }
        String description;
        List<Method> methods = resource.getMethods();
        Map<String, Tag> tags = new HashMap<String, Tag>();

        List<SecurityRequirement> resourceSecurities = new ArrayList<SecurityRequirement>();

        // Add the description from the controller api
        Class<?> controller = resource.getControllerClass();
        RequestMapping controllerRM = controller.getAnnotation(RequestMapping.class);


        String[] controllerProduces = new String[0];
        String[] controllerConsumes = new String[0];
        if (controllerRM != null) {
            controllerConsumes = controllerRM.consumes();
            controllerProduces = controllerRM.produces();
        }

        if (controller != null && controller.isAnnotationPresent(Api.class)) {
            Api api = controller.getAnnotation(Api.class);
            if (!canReadApi(false, api)) {
                return swagger;
            }
            tags = updateTagsForApi(null, api);
            resourceSecurities = getSecurityRequirements(api);
            description = api.description();
        }

        resourcePath = resource.getControllerMapping();

        //collect api from method with @RequestMapping
        Map<String, List<Method>> apiMethodMap = collectApisByRequestMapping(methods);

        for (String path : apiMethodMap.keySet()) {
            for (Method method : apiMethodMap.get(path)) {

                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                if (requestMapping == null) {
                    continue;
                }
                ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);
                if (apiOperation == null) {
                    continue;
                }
                String httpMethod = null;


                Map<String, String> regexMap = new HashMap<String, String>();
                String operationPath = parseOperationPath(path, regexMap);

                //http method
                if (requestMapping.method() != null && requestMapping.method().length != 0) {
                    httpMethod = requestMapping.method()[0].toString().toLowerCase();
                    if (httpMethod == null) {
                        continue;
                    }
                }

                Operation operation = parseMethod(method);

                updateOperationParameters(new ArrayList<Parameter>(), regexMap, operation);

                updateOperationProtocols(apiOperation, operation);

                String[] apiProduces = requestMapping.produces();
                String[] apiConsumes = requestMapping.consumes();

                apiProduces = (apiProduces == null || apiProduces.length == 0 ) ? controllerProduces : apiProduces;
                apiConsumes = (apiConsumes == null || apiProduces.length == 0 ) ? controllerConsumes : apiConsumes;

                apiConsumes = updateOperationConsumes(new String[0], apiConsumes, operation);
                apiProduces = updateOperationProduces(new String[0], apiProduces, operation);

                ApiOperation op = method.getAnnotation(ApiOperation.class);
                updateTagsForOperation(operation, op);
                updateOperation(apiConsumes, apiProduces, tags, resourceSecurities, operation);
                updatePath(operationPath, httpMethod, operation);


            }

        }
        return swagger;
    }


//    private void handleSubResource(String[] apiConsumes, String httpMethod, String[] apiProduces, Map<String, Tag> tags, Method method, String operationPath, Operation operation) {
//        if (isSubResource(method)) {
//            Type t = method.getGenericReturnType();
//            Class<?> responseClass = method.getReturnType();
//            Swagger subSwagger = read(responseClass, operationPath,httpMethod, true, apiConsumes, apiProduces, tags, operation.getParameters());
//        }
//    }


    private Operation parseMethod(Method method) {
        Operation operation = new Operation();

        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        Class<?> responseClass = null;
        List<String> produces = new ArrayList<String>();
        List<String> consumes = new ArrayList<String>();
        String responseContainer = null;
        String operationId = method.getName();
        Map<String, Property> defaultResponseHeaders;

        ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);

        if (apiOperation.hidden())
            return null;
        if (!"".equals(apiOperation.nickname()))
            operationId = method.getName();

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
            // pick out response from method declaration
            LOG.info("picking up response class from method " + method);
            Type t = method.getGenericReturnType();
            responseClass = method.getReturnType();
            if (responseClass.equals(ResponseEntity.class)) {
                responseClass = getGenericSubtype(method.getReturnType(), method.getGenericReturnType());
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
            for (String str : Arrays.asList(requestMapping.consumes())) {
                if (!consumes.contains(str)) {
                    consumes.add(str);
                }
            }
        }

        ApiResponses responseAnnotation = method.getAnnotation(ApiResponses.class);
        if (responseAnnotation != null) {
            updateApiResponse(operation, responseAnnotation);
        } else {
            ResponseStatus responseStatus = method.getAnnotation(ResponseStatus.class);
            if (responseStatus != null) {
                operation.response(responseStatus.value().value(), new Response().description(responseStatus.reason()));
            }
        }

        boolean isDeprecated = false;
        Deprecated annotation = method.getAnnotation(Deprecated.class);
        if (annotation != null)
            isDeprecated = true;

        boolean hidden = false;
        if (apiOperation != null)
            hidden = apiOperation.hidden();

        // process parameters
        Class[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
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


    private Map<String, List<Method>> collectApisByRequestMapping(List<Method> methods) {
        Map<String, List<Method>> apiMethodMap = new HashMap<String, List<Method>>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String path = "";
                if (requestMapping.value() != null && requestMapping.value().length != 0) {
                    path = generateFullPath(requestMapping.value()[0]);
                } else {
                    path = resourcePath;
                }
                if (apiMethodMap.containsKey(path)) {
                    apiMethodMap.get(path).add(method);
                } else {
                    List<Method> ms = new ArrayList<Method>();
                    ms.add(method);
                    apiMethodMap.put(path, ms);
                }
            }
        }

        return apiMethodMap;
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

    private String getModelNameFromGetterMethodName(Method method) {
        String name = null;
        if ((method.getName().startsWith("get") || method.getName().startsWith("is"))
                && !(method.getName().equals("getClass"))) {
            try {
                if (method.getName().startsWith("get")) {
                    name = method.getName().substring(3);
                } else {
                    name = method.getName().substring(2);
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

    //Helper method for loadDocuments()
    private Map<String, SpringResource> analyzeController(Class<?> clazz, Map<String, SpringResource> resourceMap, String description) throws ClassNotFoundException {

        for (int i = 0; i < clazz.getAnnotation(RequestMapping.class).value().length; i++) {
            String controllerMapping = clazz.getAnnotation(RequestMapping.class).value()[i];
            String resourceName = SprintUtils.parseResourceName(clazz);
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping methodReq = method.getAnnotation(RequestMapping.class);
                    if (methodReq.value() == null || methodReq.value().length == 0 || SprintUtils.parseResourceName(methodReq.value()[0]).equals("")) {
                        if (resourceName.length() != 0) {
                            String resourceKey = SprintUtils.createResourceKey(resourceName, SprintUtils.parseVersion(controllerMapping));
                            if ((!(resourceMap.containsKey(resourceKey)))) {
                                resourceMap.put(resourceKey, new SpringResource(clazz, resourceName, resourceKey, description));
                            }
                            resourceMap.get(resourceKey).addMethod(method);
                        }
                    }
                }
            }
        }
        clazz.getFields();
        clazz.getDeclaredFields(); //<--In case developer declares a field without an associated getter/setter.
        //this will allow NoClassDefFoundError to be caught before it triggers bamboo failure.

        return resourceMap;
    }

    private Map<String, SpringResource> generateResourceMap(Set<Class<?>> validClasses) throws GenerateException {
        Map<String, SpringResource> resourceMap = new HashMap<String, SpringResource>();
        for (Class<?> c : validClasses) {
            RequestMapping requestMapping = c.getAnnotation(RequestMapping.class);
            String description = "";
            if (requestMapping != null && requestMapping.value().length != 0) {
                //This try/catch block is to stop a bamboo build from failing due to NoClassDefFoundError
                //This occurs when a class or method loaded by reflections contains a type that has no dependency
                try {
                    resourceMap = analyzeController(c, resourceMap, description);
                    List<Method> mList = new ArrayList<Method>(Arrays.asList(c.getMethods()));
                    if (c.getSuperclass() != null) {
                        mList.addAll(Arrays.asList(c.getSuperclass().getMethods()));
                    }
                    for (Method method : mList) {
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping methodReq = method.getAnnotation(RequestMapping.class);
                            //isolate resource name - attempt first by the first part of the mapping
                            if (methodReq != null && methodReq.value().length != 0) {
                                for (int i = 0; i < methodReq.value().length; i++) {
                                    String resourceKey = "";
                                    String resourceName = SprintUtils.parseResourceName(methodReq.value()[i]);
                                    if (!(resourceName.equals(""))) {
                                        String version = SprintUtils.parseVersion(requestMapping.value()[0]);
                                        //get version - first try by class mapping, then method
                                        if (version.equals("")) {
                                            //class mapping failed - use method
                                            version = SprintUtils.parseVersion(methodReq.value()[i]);
                                        }
                                        resourceKey = SprintUtils.createResourceKey(resourceName, version);
                                        if ((!(resourceMap.containsKey(resourceKey)))) {
                                            resourceMap.put(resourceKey, new SpringResource(c, resourceName, resourceKey, description));
                                        }
                                        resourceMap.get(resourceKey).addMethod(method);
                                    }
                                }
                            }
                        }
                    }
                } catch (NoClassDefFoundError e) {
                    LOG.error(e.getMessage());
                    LOG.info(c.getName());
                    //exception occurs when a method type or annotation is not recognized by the plugin
                } catch (ClassNotFoundException e) {
                    LOG.error(e.getMessage());
                    LOG.info(c.getName());
                }

            }
        }
        return resourceMap;
    }

}
