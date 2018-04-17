package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import com.github.kongchen.swagger.docgen.spring.SpringSwaggerExtension;
import com.github.kongchen.swagger.docgen.util.SpringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

public class SpringMvcApiReader extends AbstractReader implements ClassSwaggerReader {
    private static final ResponseContainerConverter RESPONSE_CONTAINER_CONVERTER = new ResponseContainerConverter();

    private final SpringExceptionHandlerReader exceptionHandlerReader;

    private String resourcePath;

    public SpringMvcApiReader(final Swagger swagger, final Log log) {
        super(swagger, log);
        exceptionHandlerReader = new SpringExceptionHandlerReader(log);
    }

    @Override
    protected void updateExtensionChain() {
    	final List<SwaggerExtension> extensions = new ArrayList<SwaggerExtension>();
    	extensions.add(new SpringSwaggerExtension());
    	SwaggerExtensions.setExtensions(extensions);
    }

    @Override
    public Swagger read(final Set<Class<?>> classes) throws GenerateException {
        //relate all methods to one base request mapping if multiple controllers exist for that mapping
        //get all methods from each controller & find their request mapping
        //create map - resource string (after first slash) as key, new SpringResource as value
        final Map<String, SpringResource> resourceMap = generateResourceMap(classes);
        exceptionHandlerReader.processExceptionHandlers(classes);
        for (final SpringResource resource: resourceMap.values()) {
            read(resource);
        }

        return swagger;
    }

    public Swagger read(final SpringResource resource) {
        if (swagger == null) {
            swagger = new Swagger();
        }
        final List<Method> methods = resource.getMethods();
        Map<String, Tag> tags = new HashMap<String, Tag>();

        List<SecurityRequirement> resourceSecurities = new ArrayList<SecurityRequirement>();

        // Add the description from the controller api
        final Class<?> controller = resource.getControllerClass();
        final RequestMapping controllerRM = findMergedAnnotation(controller, RequestMapping.class);

        String[] controllerProduces = new String[0];
        String[] controllerConsumes = new String[0];
        if (controllerRM != null) {
            controllerConsumes = controllerRM.consumes();
            controllerProduces = controllerRM.produces();
        }

        if (controller.isAnnotationPresent(Api.class)) {
            final Api api = findMergedAnnotation(controller, Api.class);
            if (!canReadApi(false, api)) {
                return swagger;
            }
            tags = updateTagsForApi(null, api);
            resourceSecurities = getSecurityRequirements(api);
        }

        resourcePath = resource.getControllerMapping();

        //collect api from method with @RequestMapping
        final Map<String, List<Method>> apiMethodMap = collectApisByRequestMapping(methods);

        for (final String path : apiMethodMap.keySet()) {
            for (final Method method : apiMethodMap.get(path)) {
                final RequestMapping requestMapping = findMergedAnnotation(method, RequestMapping.class);
                if (requestMapping == null) {
                    continue;
                }
                final ApiOperation apiOperation = findMergedAnnotation(method, ApiOperation.class);
                if (apiOperation != null && apiOperation.hidden()) {
                    continue;
                }

                final Map<String, String> regexMap = new HashMap<String, String>();
                final String operationPath = parseOperationPath(path, regexMap);

                //http method
                for (final RequestMethod requestMethod : requestMapping.method()) {
                    final String httpMethod = requestMethod.toString().toLowerCase();
                    final Operation operation = parseMethod(method);

                    updateOperationParameters(new ArrayList<Parameter>(), regexMap, operation);

                    updateOperationProtocols(apiOperation, operation);

                    String[] apiProduces = requestMapping.produces();
                    String[] apiConsumes = requestMapping.consumes();

                    apiProduces = (apiProduces.length == 0) ? controllerProduces : apiProduces;
                    apiConsumes = (apiConsumes.length == 0) ? controllerConsumes : apiConsumes;

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

    private Operation parseMethod(final Method method) {
        int responseCode = 200;
        final Operation operation = new Operation();

        final RequestMapping requestMapping = findMergedAnnotation(method, RequestMapping.class);
        Type responseClass = null;
        final List<String> produces = new ArrayList<String>();
        final List<String> consumes = new ArrayList<String>();
        String responseContainer = null;
        String operationId = method.getName();
        Map<String, Property> defaultResponseHeaders = null;

        final ApiOperation apiOperation = findMergedAnnotation(method, ApiOperation.class);

        if(apiOperation != null) {
            if (apiOperation.hidden()) {
                return null;
            }
            if (!apiOperation.nickname().isEmpty()) {
                operationId = apiOperation.nickname();
            }

            defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders());

            operation.summary(apiOperation.value()).description(apiOperation.notes());

            final Set<Map<String, Object>> customExtensions = parseCustomExtensions(apiOperation.extensions());

            for (final Map<String, Object> extension : customExtensions) {
                if (extension == null) {
                    continue;
                }
                for (final Map.Entry<String, Object> map : extension.entrySet()) {
                    operation.setVendorExtension(
                            map.getKey().startsWith("x-")
                                    ? map.getKey()
                                    : "x-" + map.getKey(), map.getValue()
                    );
                }
            }

            if (!apiOperation.response().equals(Void.class)) {
                responseClass = apiOperation.response();
            }
            if (!apiOperation.responseContainer().isEmpty()) {
                responseContainer = apiOperation.responseContainer();
            }

            ///security
            final List<SecurityRequirement> securities = new ArrayList<SecurityRequirement>();
            for (final Authorization auth : apiOperation.authorizations()) {
                if (!auth.value().isEmpty()) {
                    final SecurityRequirement security = new SecurityRequirement();
                    security.setName(auth.value());
                    for (final AuthorizationScope scope : auth.scopes()) {
                        if (!scope.scope().isEmpty()) {
                            security.addScope(scope.scope());
                        }
                    }
                    securities.add(security);
                }
            }
            for (final SecurityRequirement sec : securities) {
                operation.security(sec);
            }

            responseCode = apiOperation.code();
        }

        if (responseClass == null) {
            // pick out response from method declaration
            LOG.info("picking up response class from method " + method);
            responseClass = method.getGenericReturnType();
        }
        if (responseClass instanceof ParameterizedType && ResponseEntity.class.equals(((ParameterizedType) responseClass).getRawType())) {
            responseClass = ((ParameterizedType) responseClass).getActualTypeArguments()[0];
        }
        boolean hasApiAnnotation = false;
        if (responseClass instanceof Class) {
            hasApiAnnotation = findAnnotation((Class) responseClass, Api.class) != null;
        }
        if (responseClass != null
                && !responseClass.equals(Void.class)
                && !responseClass.equals(ResponseEntity.class)
                && !hasApiAnnotation) {
            if (isPrimitive(responseClass)) {
                final Property property = ModelConverters.getInstance().readAsProperty(responseClass);
                if (property != null) {
                    final Property responseProperty = RESPONSE_CONTAINER_CONVERTER.withResponseContainer(responseContainer, property);
                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                }
            } else if (!responseClass.equals(Void.class) && !responseClass.equals(void.class)) {
                final Map<String, Model> models = ModelConverters.getInstance().read(responseClass);
                if (models.isEmpty()) {
                    final Property pp = ModelConverters.getInstance().readAsProperty(responseClass);
                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(pp)
                            .headers(defaultResponseHeaders));
                }
                for (final String key : models.keySet()) {
                    final Property responseProperty = RESPONSE_CONTAINER_CONVERTER.withResponseContainer(responseContainer, new RefProperty().asDefault(key));
                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                    swagger.model(key, models.get(key));
                }
            }
            final Map<String, Model> models = ModelConverters.getInstance().readAll(responseClass);
            for (final Map.Entry<String, Model> entry : models.entrySet()) {
                swagger.model(entry.getKey(), entry.getValue());
            }
        }

        operation.operationId(operationId);

        for (final String str : requestMapping.produces()) {
            if (!produces.contains(str)) {
                produces.add(str);
            }
        }
        for (final String str : requestMapping.consumes()) {
            if (!consumes.contains(str)) {
                consumes.add(str);
            }
        }

        final ApiResponses responseAnnotation = findMergedAnnotation(method, ApiResponses.class);
        if (responseAnnotation != null) {
            updateApiResponse(operation, responseAnnotation);
        } else {
            final ResponseStatus responseStatus = findMergedAnnotation(method, ResponseStatus.class);
            if (responseStatus != null) {
                operation.response(responseStatus.value().value(), new Response().description(responseStatus.reason()));
            }
        }

        final List<ResponseStatus> errorResponses = exceptionHandlerReader.getResponseStatusesFromExceptions(method);
        for (final ResponseStatus responseStatus: errorResponses) {
            final int code = responseStatus.code().value();
            final String description = defaultIfEmpty(responseStatus.reason(), responseStatus.code().getReasonPhrase());
            operation.response(code, new Response().description(description));
        }


        final Deprecated annotation = findAnnotation(method, Deprecated.class);
        if (annotation != null) {
            operation.deprecated(true);
        }

        // process parameters
        final Class[] parameterTypes = method.getParameterTypes();
        final Type[] genericParameterTypes = method.getGenericParameterTypes();
        final Annotation[][] paramAnnotations = method.getParameterAnnotations();
        final DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
        final String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        // paramTypes = method.getParameterTypes
        // genericParamTypes = method.getGenericParameterTypes
        for (int i = 0; i < parameterTypes.length; i++) {
            final Type type = genericParameterTypes[i];
            final List<Annotation> annotations = Arrays.asList(paramAnnotations[i]);
            final List<Parameter> parameters = getParameters(type, annotations);

            for (final Parameter parameter : parameters) {
                if(parameter.getName().isEmpty()) {
                    parameter.setName(parameterNames[i]);
                }
                operation.parameter(parameter);
            }
        }

        if (operation.getResponses() == null) {
            operation.defaultResponse(new Response().description("successful operation"));
        }

        // Process @ApiImplicitParams
        this.readImplicitParameters(method, operation);

        processOperationDecorator(operation, method);

        return operation;
    }

    private Map<String, List<Method>> collectApisByRequestMapping(final List<Method> methods) {
        final Map<String, List<Method>> apiMethodMap = new HashMap<String, List<Method>>();
        for (final Method method : methods) {
            final RequestMapping requestMapping = findMergedAnnotation(method, RequestMapping.class);
            if (requestMapping != null) {
                final String path;
                if (requestMapping.value().length != 0) {
                    path = generateFullPath(requestMapping.value()[0]);
                } else {
                    path = resourcePath;
                }
                if (apiMethodMap.containsKey(path)) {
                    apiMethodMap.get(path).add(method);
                } else {
                    final List<Method> ms = new ArrayList<Method>();
                    ms.add(method);
                    apiMethodMap.put(path, ms);
                }
            }
        }

        return apiMethodMap;
    }

    private String generateFullPath(final String path) {
        if (StringUtils.isNotEmpty(path)) {
            return this.resourcePath + (path.startsWith("/") ? path : '/' + path);
        } else {
            return this.resourcePath;
        }
    }

    //Helper method for loadDocuments()
    private Map<String, SpringResource> analyzeController(final Class<?> controllerClazz, final Map<String, SpringResource> resourceMap, final String description) {
	final String[] controllerRequestMappingValues = SpringUtils.getControllerResquestMapping(controllerClazz);

        // Iterate over all value attributes of the class-level RequestMapping annotation
        for (final String controllerRequestMappingValue : controllerRequestMappingValues) {
            for (final Method method : controllerClazz.getMethods()) {
                final RequestMapping methodRequestMapping = findMergedAnnotation(method, RequestMapping.class);

                // Look for method-level @RequestMapping annotation
                if (methodRequestMapping != null) {
                    final RequestMethod[] requestMappingRequestMethods = methodRequestMapping.method();

                    // For each method-level @RequestMapping annotation, iterate over HTTP Verb
                    for (final RequestMethod requestMappingRequestMethod : requestMappingRequestMethods) {
                        final String[] methodRequestMappingValues = methodRequestMapping.value();

                        // Check for cases where method-level @RequestMapping#value is not set, and use the controllers @RequestMapping
                        if (methodRequestMappingValues.length == 0) {
                            // The map key is a concat of the following:
                            //   1. The controller package
                            //   2. The controller class name
                            //   3. The controller-level @RequestMapping#value
                            final String resourceKey = controllerClazz.getCanonicalName() + controllerRequestMappingValue + requestMappingRequestMethod;
                            if (!resourceMap.containsKey(resourceKey)) {
                                resourceMap.put(
                                        resourceKey,
                                        new SpringResource(controllerClazz, controllerRequestMappingValue, resourceKey, description));
                            }
                            resourceMap.get(resourceKey).addMethod(method);
                        } else {
                            // Here we know that method-level @RequestMapping#value is populated, so
                            // iterate over all the @RequestMapping#value attributes, and add them to the resource map.
                            for (final String methodRequestMappingValue : methodRequestMappingValues) {
                                final String resourceKey = controllerClazz.getCanonicalName() + controllerRequestMappingValue
                                        + methodRequestMappingValue + requestMappingRequestMethod;
                                if (!methodRequestMappingValue.isEmpty()) {
                                    if (!resourceMap.containsKey(resourceKey)) {
                                        resourceMap.put(resourceKey, new SpringResource(controllerClazz, methodRequestMappingValue, resourceKey, description));
                                    }
                                    resourceMap.get(resourceKey).addMethod(method);
                                }
                            }
                        }
                    }
                }
            }
        }
        controllerClazz.getFields();
        controllerClazz.getDeclaredFields(); //<--In case developer declares a field without an associated getter/setter.
        //this will allow NoClassDefFoundError to be caught before it triggers bamboo failure.

        return resourceMap;
    }

    protected Map<String, SpringResource> generateResourceMap(final Set<Class<?>> validClasses) throws GenerateException {
        Map<String, SpringResource> resourceMap = new HashMap<String, SpringResource>();
        for (final Class<?> aClass : validClasses) {
            final RequestMapping requestMapping = findAnnotation(aClass, RequestMapping.class);
            //This try/catch block is to stop a bamboo build from failing due to NoClassDefFoundError
            //This occurs when a class or method loaded by reflections contains a type that has no dependency
            try {
                resourceMap = analyzeController(aClass, resourceMap, "");
                final List<Method> mList = new ArrayList<Method>(Arrays.asList(aClass.getMethods()));
                if (aClass.getSuperclass() != null) {
                    mList.addAll(Arrays.asList(aClass.getSuperclass().getMethods()));
                }
            } catch (final NoClassDefFoundError e) {
                LOG.error(e.getMessage());
                LOG.info(aClass.getName());
                //exception occurs when a method type or annotation is not recognized by the plugin
            }
        }

        return resourceMap;
    }

}
