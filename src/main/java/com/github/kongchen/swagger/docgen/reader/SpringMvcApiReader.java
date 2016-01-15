package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import io.swagger.annotations.*;
import io.swagger.converter.ModelConverters;
import io.swagger.models.*;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class SpringMvcApiReader extends AbstractReader implements ClassSwaggerReader {
    private String resourcePath;

    public SpringMvcApiReader(Swagger swagger, LogAdapter log) {
        super(swagger, log);

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
        List<Method> methods = resource.getMethods();
        Map<String, Tag> tags = new HashMap<String, Tag>();

        List<SecurityRequirement> resourceSecurities = new ArrayList<SecurityRequirement>();

        // Add the description from the controller api
        Class<?> controller = resource.getControllerClass();
        RequestMapping controllerRM = AnnotationUtils.findAnnotation(controller, RequestMapping.class);


        String[] controllerProduces = new String[0];
        String[] controllerConsumes = new String[0];
        if (controllerRM != null) {
            controllerConsumes = controllerRM.consumes();
            controllerProduces = controllerRM.produces();
        }

        if (controller != null && controller.isAnnotationPresent(Api.class)) {
            Api api = AnnotationUtils.findAnnotation(controller, Api.class);
            if (!canReadApi(false, api)) {
                return swagger;
            }
            tags = updateTagsForApi(null, api);
            resourceSecurities = getSecurityRequirements(api);
        }

        resourcePath = resource.getControllerMapping();

        //collect api from method with @RequestMapping
        Map<String, List<Method>> apiMethodMap = collectApisByRequestMapping(methods);

        for (String path : apiMethodMap.keySet()) {
            for (Method method : apiMethodMap.get(path)) {

                RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
                if (requestMapping == null) {
                    continue;
                }
                ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
                if (apiOperation == null || apiOperation.hidden()) {
                    continue;
                }
                String httpMethod = null;


                Map<String, String> regexMap = new HashMap<String, String>();
                String operationPath = parseOperationPath(path, regexMap);

                //http method
                for (RequestMethod requestMethod : requestMapping.method()) {
                    httpMethod = requestMethod.toString().toLowerCase();
                    Operation operation = parseMethod(method);

                    updateOperationParameters(new ArrayList<Parameter>(), regexMap, operation);

                    updateOperationProtocols(apiOperation, operation);

                    String[] apiProduces = requestMapping.produces();
                    String[] apiConsumes = requestMapping.consumes();

                    apiProduces = (apiProduces == null || apiProduces.length == 0) ? controllerProduces : apiProduces;
                    apiConsumes = (apiConsumes == null || apiProduces.length == 0) ? controllerConsumes : apiConsumes;

                    apiConsumes = updateOperationConsumes(new String[0], apiConsumes, operation);
                    apiProduces = updateOperationProduces(new String[0], apiProduces, operation);

                    updateTagsForOperation(operation, apiOperation);
                    updateOperation(apiConsumes, apiProduces, tags, resourceSecurities, operation);
                    updatePath(operationPath, httpMethod, operation);
                }
            }

        }
        return swagger;
    }

    private Operation parseMethod(Method method) {
        Operation operation = new Operation();

        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        Class<?> responseClass = null;
        List<String> produces = new ArrayList<String>();
        List<String> consumes = new ArrayList<String>();
        String responseContainer = null;
        String operationId = method.getName();
        Map<String, Property> defaultResponseHeaders = null;
        Set<Map<String, Object>> customExtensions = null;

        ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);

        if (apiOperation.hidden())
            return null;
        if (!"".equals(apiOperation.nickname()))
            operationId = apiOperation.nickname();

        defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders());

        operation.summary(apiOperation.value()).description(apiOperation.notes());

        customExtensions = parseCustomExtensions(apiOperation.extensions());
        if (customExtensions != null) {
            for (Map<String, Object> extension : customExtensions) {
                if (extension != null) {
                    for (Map.Entry<String, Object> map : extension.entrySet()) {
                        operation.setVendorExtension(map.getKey().startsWith("x-") ? map.getKey() : "x-" + map.getKey(), map.getValue());
                    }
                }
            }
        }

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
                        if (scope.scope() != null && !"".equals(scope.scope())) {
                            security.addScope(scope.scope());
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
            if (!responseClass.equals(Void.class) && !"void".equals(responseClass.toString())
                    && AnnotationUtils.findAnnotation(responseClass, Api.class) == null) {
                LOG.info("reading model " + responseClass);
                Map<String, Model> models = ModelConverters.getInstance().readAll(t);
            }
        }
        if (responseClass != null
                && !responseClass.equals(Void.class)
                && !responseClass.equals(ResponseEntity.class)
                && AnnotationUtils.findAnnotation(responseClass, Api.class) == null) {
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
                    operation.response(apiOperation.code(), new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                }
            } else if (!responseClass.equals(Void.class) && !"void".equals(responseClass.toString())) {
                Map<String, Model> models = ModelConverters.getInstance().read(responseClass);
                if (models.size() == 0) {
                    Property pp = ModelConverters.getInstance().readAsProperty(responseClass);
                    operation.response(apiOperation.code(), new Response()
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
                    operation.response(apiOperation.code(), new Response()
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

        ApiResponses responseAnnotation = AnnotationUtils.findAnnotation(method, ApiResponses.class);
        if (responseAnnotation != null) {
            updateApiResponse(operation, responseAnnotation);
        } else {
            ResponseStatus responseStatus = AnnotationUtils.findAnnotation(method, ResponseStatus.class);
            if (responseStatus != null) {
                operation.response(responseStatus.value().value(), new Response().description(responseStatus.reason()));
            }
        }

        Deprecated annotation = AnnotationUtils.findAnnotation(method, Deprecated.class);
        if (annotation != null)
            operation.deprecated(true);

        // FIXME `hidden` is never used
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
            Type type = genericParameterTypes[i];
            List<Annotation> annotations = Arrays.asList(paramAnnotations[i]);
            List<Parameter> parameters = getParameters(type, annotations);

            for (Parameter parameter : parameters) {
                operation.parameter(parameter);
            }
        }

        if (operation.getResponses() == null) {
            operation.defaultResponse(new Response().description("successful operation"));
        }

        // Process @ApiImplicitParams
        this.readImplicitParameters(method, operation);

        return operation;

    }

    private Map<String, List<Method>> collectApisByRequestMapping(List<Method> methods) {
        Map<String, List<Method>> apiMethodMap = new HashMap<String, List<Method>>();
        for (Method method : methods) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
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

    private String generateFullPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            return this.resourcePath + (path.startsWith("/") ? path : '/' + path);
        } else {
            return this.resourcePath;
        }
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

    //Helper method for loadDocuments()
    private Map<String, SpringResource> analyzeController(Class<?> clazz, Map<String, SpringResource> resourceMap, String description) throws ClassNotFoundException {
        String controllerCanonicalName = clazz.getCanonicalName();
        String[] controllerRequestMappingValues = null;

        //Determine if we will use class-level requestmapping or dummy string
        if (AnnotationUtils.findAnnotation(clazz, RequestMapping.class) != null
                && AnnotationUtils.findAnnotation(clazz, RequestMapping.class).value() != null) {
            controllerRequestMappingValues = AnnotationUtils.findAnnotation(clazz, RequestMapping.class).value();
        } else {
            controllerRequestMappingValues = new String[1];
            controllerRequestMappingValues[0] = "";
        }

        // Iterate over all value attributes of the class-level RequestMapping annotation
        for (int i = 0; i < controllerRequestMappingValues.length; i++) {

            // Iterate over all methods inside the controller
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                RequestMapping methodRequestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);

                // Look for method-level @RequestMapping annotation
                if (methodRequestMapping instanceof RequestMapping) {
                    RequestMethod[] requestMappingRequestMethods = methodRequestMapping.method();

                    // For each method-level @RequestMapping annotation, iterate over HTTP Verb
                    for (RequestMethod requestMappingRequestMethod : requestMappingRequestMethods) {
                        String[] methodRequestMappingValues = methodRequestMapping.value();

                        // Check for cases where method-level @RequestMapping#value is not set, and use the controllers @RequestMapping
                        if (methodRequestMappingValues == null || methodRequestMappingValues.length == 0) {
                            // The map key is a concat of the following:
                            //   1. The controller package
                            //   2. The controller class name
                            //   3. The controller-level @RequestMapping#value
                            String resourceKey = controllerCanonicalName + controllerRequestMappingValues[i] + requestMappingRequestMethod;
                            if ((!(resourceMap.containsKey(resourceKey)))) {
                                resourceMap.put(
                                        resourceKey,
                                        new SpringResource(clazz, controllerRequestMappingValues[i], resourceKey, description));
                            }
                            resourceMap.get(resourceKey).addMethod(method);
                        } else {
                            // Here we know that method-level @RequestMapping#value is populated, so
                            // iterate over all the @RequestMapping#value attributes, and add them to the resource map.
                            for (String methodRequestMappingValue : methodRequestMappingValues) {
                                String resourceName = methodRequestMappingValue;
                                // The map key is a concat of the following:
                                //   1. The controller package
                                //   2. The controller class name
                                //   3. The controller-level @RequestMapping#value
                                //   4. The method-level @RequestMapping#value
                                //   5. The method-level @RequestMapping#method
                                String resourceKey = controllerCanonicalName + controllerRequestMappingValues[i] + resourceName + requestMappingRequestMethod;
                                if (!(resourceName.equals(""))) {
                                    if ((!(resourceMap.containsKey(resourceKey)))) {
                                        resourceMap.put(resourceKey, new SpringResource(clazz, resourceName, resourceKey, description));
                                    }
                                    resourceMap.get(resourceKey).addMethod(method);
                                }
                            }
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

    protected Map<String, SpringResource> generateResourceMap(Set<Class<?>> validClasses) throws GenerateException {
        Map<String, SpringResource> resourceMap = new HashMap<String, SpringResource>();
        for (Class<?> c : validClasses) {
            RequestMapping requestMapping = AnnotationUtils.findAnnotation(c, RequestMapping.class);
            String description = "";
            //This try/catch block is to stop a bamboo build from failing due to NoClassDefFoundError
            //This occurs when a class or method loaded by reflections contains a type that has no dependency
            try {
                resourceMap = analyzeController(c, resourceMap, description);
                List<Method> mList = new ArrayList<Method>(Arrays.asList(c.getMethods()));
                if (c.getSuperclass() != null) {
                    mList.addAll(Arrays.asList(c.getSuperclass().getMethods()));
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

        return resourceMap;
    }

}
