package com.github.kongchen.swagger.docgen.reader;

import com.fasterxml.jackson.databind.type.TypeFactory;
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
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.apache.maven.plugin.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JaxrsReader extends AbstractReader implements ClassSwaggerReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaxrsReader.class);

    public JaxrsReader(Swagger swagger, Log LOG) {
        super(swagger, LOG);
    }

    @Override
    public Swagger read(Set<Class<?>> classes) {
        for (Class cls : classes) {
            read(cls);
        }
        return swagger;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public Swagger read(Class cls) {
        return read(cls, "", null, false, new String[0], new String[0], new HashMap<String, Tag>(), new ArrayList<Parameter>());
    }

    protected Swagger read(Class<?> cls, String parentPath, String parentMethod, boolean readHidden, String[] parentConsumes, String[] parentProduces, Map<String, Tag> parentTags, List<Parameter> parentParameters) {
        if (swagger == null) {
            swagger = new Swagger();
        }
        Api api = AnnotationUtils.findAnnotation(cls, Api.class);
        Path apiPath = AnnotationUtils.findAnnotation(cls, Path.class);

        // only read if allowing hidden apis OR api is not marked as hidden
        if (!canReadApi(readHidden, api)) {
            return swagger;
        }

        Map<String, Tag> tags = updateTagsForApi(parentTags, api);
        List<SecurityRequirement> securities = getSecurityRequirements(api);

        // merge consumes, pro duces

        // look for method-level annotated properties

        // handle subresources by looking at return type

        // parse the method
        for (Method method : cls.getMethods()) {
            ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
            if (apiOperation == null || apiOperation.hidden()) {
                continue;
            }
            Path methodPath = AnnotationUtils.findAnnotation(method, Path.class);

            String operationPath = getPath(apiPath, methodPath, parentPath);
            if (operationPath != null) {
                Map<String, String> regexMap = new HashMap<String, String>();
                operationPath = parseOperationPath(operationPath, regexMap);

                String httpMethod = extractOperationMethod(apiOperation, method, SwaggerExtensions.chain());

                Operation operation = parseMethod(method);
                updateOperationParameters(parentParameters, regexMap, operation);
                updateOperationProtocols(apiOperation, operation);

                String[] apiConsumes = new String[0];
                String[] apiProduces = new String[0];

                Consumes consumes = AnnotationUtils.getAnnotation(cls, Consumes.class);
                if (consumes != null) {
                    apiConsumes = consumes.value();
                }
                Produces produces = AnnotationUtils.getAnnotation(cls, Produces.class);
                if (produces != null) {
                    apiProduces = produces.value();
                }

                apiConsumes = updateOperationConsumes(parentConsumes, apiConsumes, operation);
                apiProduces = updateOperationProduces(parentProduces, apiProduces, operation);

                handleSubResource(apiConsumes, httpMethod, apiProduces, tags, method, operationPath, operation);

                // can't continue without a valid http method
                httpMethod = (httpMethod == null) ? parentMethod : httpMethod;
                updateTagsForOperation(operation, apiOperation);
                updateOperation(apiConsumes, apiProduces, tags, securities, operation);
                updatePath(operationPath, httpMethod, operation);
            }
        }

        return swagger;
    }

    private void handleSubResource(String[] apiConsumes, String httpMethod, String[] apiProduces, Map<String, Tag> tags, Method method, String operationPath, Operation operation) {
        if (isSubResource(method)) {
            Class<?> responseClass = method.getReturnType();
            Swagger subSwagger = read(responseClass, operationPath, httpMethod, true, apiConsumes, apiProduces, tags, operation.getParameters());
        }
    }

    protected boolean isSubResource(Method method) {
        Class<?> responseClass = method.getReturnType();
        return (responseClass != null) && (AnnotationUtils.findAnnotation(responseClass, Api.class) != null);
    }

    private String getPath(Path classLevelPath, Path methodLevelPath, String parentPath) {
        if (classLevelPath == null && methodLevelPath == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (parentPath != null && !parentPath.isEmpty() && !parentPath.equals("/")) {
            if (!parentPath.startsWith("/")) {
                parentPath = "/" + parentPath;
            }
            if (parentPath.endsWith("/")) {
                parentPath = parentPath.substring(0, parentPath.length() - 1);
            }

            stringBuilder.append(parentPath);
        }
        if (classLevelPath != null) {
            stringBuilder.append(classLevelPath.value());
        }
        if (methodLevelPath != null && !methodLevelPath.value().equals("/")) {
            String methodPath = methodLevelPath.value();
            if (!methodPath.startsWith("/") && !stringBuilder.toString().endsWith("/")) {
                stringBuilder.append("/");
            }
            if (methodPath.endsWith("/")) {
                methodPath = methodPath.substring(0, methodPath.length() - 1);
            }
            stringBuilder.append(methodPath);
        }
        String output = stringBuilder.toString();
        if (!output.startsWith("/")) {
            output = "/" + output;
        }
        if (output.endsWith("/") && output.length() > 1) {
            return output.substring(0, output.length() - 1);
        } else {
            return output;
        }
    }

    public Operation parseMethod(Method method) {
        Operation operation = new Operation();
        ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);

        String operationId = method.getName();
        String responseContainer = null;

        Type responseType = null;
        Map<String, Property> defaultResponseHeaders = null;

        if (apiOperation != null) {
            if (apiOperation.hidden()) {
                return null;
            }
            if (!apiOperation.nickname().isEmpty()) {
                operationId = apiOperation.nickname();
            }

            defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders());
            operation.summary(apiOperation.value()).description(apiOperation.notes());

            Set<Map<String, Object>> customExtensions = parseCustomExtensions(apiOperation.extensions());
            if (customExtensions != null) {
                for (Map<String, Object> extension : customExtensions) {
                    if (extension == null) {
                        continue;
                    }
                    for (Map.Entry<String, Object> map : extension.entrySet()) {
                        operation.setVendorExtension(map.getKey().startsWith("x-") ? map.getKey() : "x-" + map.getKey(), map.getValue());
                    }
                }
            }

            if (!apiOperation.response().equals(Void.class)) {
                responseType = apiOperation.response();
            }
            if (!apiOperation.responseContainer().isEmpty()) {
                responseContainer = apiOperation.responseContainer();
            }
            List<SecurityRequirement> securities = new ArrayList<SecurityRequirement>();
            for (Authorization auth : apiOperation.authorizations()) {
                if (!auth.value().isEmpty()) {
                    SecurityRequirement security = new SecurityRequirement();
                    security.setName(auth.value());
                    for (AuthorizationScope scope : auth.scopes()) {
                        if (!scope.scope().isEmpty()) {
                            security.addScope(scope.scope());
                        }
                    }
                    securities.add(security);
                }
            }

            for (SecurityRequirement sec : securities) {
                operation.security(sec);
            }
        }
        operation.operationId(operationId);

        if (responseType == null) {
            // pick out response from method declaration
            LOGGER.debug("picking up response class from method " + method);
            responseType = method.getGenericReturnType();
        }
        if ((responseType != null)
                && !responseType.equals(Void.class)
                && !responseType.equals(javax.ws.rs.core.Response.class)
                && (AnnotationUtils.findAnnotation(toClass(responseType), Api.class) == null)) {
            if (isPrimitive(responseType)) {
                Property responseProperty;
                Property property = ModelConverters.getInstance().readAsProperty(responseType);
                if (property != null) {
                    if ("list".equalsIgnoreCase(responseContainer)) {
                        responseProperty = new ArrayProperty(property);
                    } else if ("map".equalsIgnoreCase(responseContainer)) {
                        responseProperty = new MapProperty(property);
                    } else {
                        responseProperty = property;
                    }
                    operation.response(apiOperation.code(), new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                }
            } else if (!responseType.equals(Void.class) && !responseType.equals(void.class)) {
                Map<String, Model> models = ModelConverters.getInstance().read(responseType);
                if (models.isEmpty()) {
                    Property p = ModelConverters.getInstance().readAsProperty(responseType);
                    operation.response(apiOperation.code(), new Response()
                            .description("successful operation")
                            .schema(p)
                            .headers(defaultResponseHeaders));
                }
                for (String key : models.keySet()) {
                    Property responseProperty;

                    if ("list".equalsIgnoreCase(responseContainer)) {
                        responseProperty = new ArrayProperty(new RefProperty().asDefault(key));
                    } else if ("map".equalsIgnoreCase(responseContainer)) {
                        responseProperty = new MapProperty(new RefProperty().asDefault(key));
                    } else {
                        responseProperty = new RefProperty().asDefault(key);
                    }
                    operation.response(apiOperation.code(), new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                    swagger.model(key, models.get(key));
                }
                models = ModelConverters.getInstance().readAll(responseType);
                for (Map.Entry<String, Model> entry : models.entrySet()) {
                    swagger.model(entry.getKey(), entry.getValue());
                }
            }
        }

        Consumes consumes = AnnotationUtils.findAnnotation(method, Consumes.class);
        if (consumes != null) {
            for (String mediaType : consumes.value()) {
                operation.consumes(mediaType);
            }
        }

        Produces produces = AnnotationUtils.findAnnotation(method, Produces.class);
        if (produces != null) {
            for (String mediaType : produces.value()) {
                operation.produces(mediaType);
            }
        }

        ApiResponses responseAnnotation = AnnotationUtils.findAnnotation(method, ApiResponses.class);
        if (responseAnnotation != null) {
            updateApiResponse(operation, responseAnnotation);
        }

        if (AnnotationUtils.findAnnotation(method, Deprecated.class) != null) {
            operation.deprecated(true);
        }

        // FIXME `hidden` is never used
        boolean hidden = false;
        if (apiOperation != null) {
            hidden = apiOperation.hidden();
        }

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

    public String extractOperationMethod(ApiOperation apiOperation, Method method, Iterator<SwaggerExtension> chain) {
        if (!apiOperation.httpMethod().isEmpty()) {
            return apiOperation.httpMethod().toLowerCase();
        } else if (AnnotationUtils.findAnnotation(method, GET.class) != null) {
            return "get";
        } else if (AnnotationUtils.findAnnotation(method, PUT.class) != null) {
            return "put";
        } else if (AnnotationUtils.findAnnotation(method, POST.class) != null) {
            return "post";
        } else if (AnnotationUtils.findAnnotation(method, DELETE.class) != null) {
            return "delete";
        } else if (AnnotationUtils.findAnnotation(method, OPTIONS.class) != null) {
            return "options";
        } else if (AnnotationUtils.findAnnotation(method, HEAD.class) != null) {
            return "head";
        } else if (AnnotationUtils.findAnnotation(method, io.swagger.jaxrs.PATCH.class) != null) {
            return "patch";
        } else {
            // check for custom HTTP Method annotations
            for (Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
                Annotation[] innerAnnotations = declaredAnnotation.annotationType().getAnnotations();
                for (Annotation innerAnnotation : innerAnnotations) {
                    if (innerAnnotation instanceof HttpMethod) {
                        HttpMethod httpMethod = (HttpMethod) innerAnnotation;
                        return httpMethod.value().toLowerCase();
                    }
                }
            }

            if (chain.hasNext()) {
                return chain.next().extractOperationMethod(apiOperation, method, chain);
            }
        }

        return null;
    }

    /**
     * Returns a Class that matches the Type given
     */
    public Class<?> toClass(Type type) {
        return TypeFactory.defaultInstance().constructType(type).getRawClass();
    }

}
