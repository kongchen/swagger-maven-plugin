package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.jaxrs.BeanParamInjectParamExtention;
import com.github.kongchen.swagger.docgen.jaxrs.JaxrsParameterExtension;
import io.swagger.annotations.*;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.jersey.SwaggerJerseyJaxrs;
import io.swagger.models.*;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.BaseReaderUtils;
import io.swagger.util.ReflectionUtils;
import org.apache.maven.plugin.logging.Log;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import javax.ws.rs.*;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class JaxrsReader extends AbstractReader implements ClassSwaggerReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaxrsReader.class);
    private static final ResponseContainerConverter RESPONSE_CONTAINER_CONVERTER = new ResponseContainerConverter();

  public JaxrsReader(Swagger swagger, Log LOG) {
        super(swagger, LOG);
    }

    /**
     * This is overridden and made public so that it can be called by
     * {@link BeanParamInjectParamExtention}.
     */
    @Override
    public List<Parameter> getParameters(Type type, List<Annotation> annotations, Set<Type> typesToSkip) {
        return super.getParameters(type, annotations, typesToSkip);
    }

    @Override
    protected void updateExtensionChain() {
    	List<SwaggerExtension> extensions = new ArrayList<SwaggerExtension>();
    	extensions.add(new BeanParamInjectParamExtention(this));
        extensions.add(new SwaggerJerseyJaxrs());
        extensions.add(new JaxrsParameterExtension());
    	SwaggerExtensions.setExtensions(extensions);
    }

    @Override
    public Swagger read(Set<Class<?>> classes) {
        for (Class<?> cls : classes) {
            read(cls);
        }
        return swagger;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public Swagger read(Class<?> cls) {
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
        Map<String, Tag> discoveredTags = scanClasspathForTags();

        // merge consumes, pro duces

        // look for method-level annotated properties

        // handle subresources by looking at return type

        // parse the method
        List<Method> filteredMethods = getFilteredMethods(cls);
        for (Method method : filteredMethods) {
            ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
            if (apiOperation != null && apiOperation.hidden()) {
                continue;
            }
            Path methodPath = AnnotationUtils.findAnnotation(method, Path.class);

            String parentPathValue = String.valueOf(parentPath);
            //is method default handler within a subresource
            if(apiPath == null && methodPath == null && parentPath != null && readHidden){
                final String updatedMethodPath = String.valueOf(parentPath);
                Path path = new Path(){
                    @Override
                    public String value(){
                        return updatedMethodPath;
                    }

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return Path.class;
                    }
                };
                methodPath = path;
                parentPathValue = null;
            }
            String operationPath = getPath(apiPath, methodPath, parentPathValue);
            if (operationPath != null) {
                Map<String, String> regexMap = new HashMap<String, String>();
                operationPath = parseOperationPath(operationPath, regexMap);

                String httpMethod = extractOperationMethod(apiOperation, method, SwaggerExtensions.chain());

                Operation operation = parseMethod(httpMethod, method);
                updateOperationParameters(parentParameters, regexMap, operation);
                updateOperationProtocols(apiOperation, operation);

                String[] apiConsumes = new String[0];
                String[] apiProduces = new String[0];

                Consumes consumes = AnnotationUtils.findAnnotation(cls, Consumes.class);
                if (consumes != null) {
                    apiConsumes = consumes.value();
                }
                Produces produces = AnnotationUtils.findAnnotation(cls, Produces.class);
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
            updateTagDescriptions(discoveredTags);
        }

        return swagger;
    }

    private List<Method> getFilteredMethods(Class<?> cls) {
        Method[] methods = cls.getMethods();
        List<Method> filteredMethods = new ArrayList<Method>();
        for (Method method : methods) {
            if (!method.isBridge()) {
                filteredMethods.add(method);
            }
        }
        return filteredMethods;
    }

    private void updateTagDescriptions(Map<String, Tag> discoveredTags) {
        if (swagger.getTags() != null) {
            for (Tag tag : swagger.getTags()) {
                Tag rightTag = discoveredTags.get(tag.getName());
                if (rightTag != null && rightTag.getDescription() != null) {
                    tag.setDescription(rightTag.getDescription());
                }
            }
        }
    }

    private Map<String, Tag> scanClasspathForTags() {
        Map<String, Tag> tags = new HashMap<String, Tag>();
        for (Class<?> aClass: new Reflections("").getTypesAnnotatedWith(SwaggerDefinition.class)) {
            SwaggerDefinition swaggerDefinition = AnnotationUtils.findAnnotation(aClass, SwaggerDefinition.class);

            for (io.swagger.annotations.Tag tag : swaggerDefinition.tags()) {

                String tagName = tag.name();
                if (!tagName.isEmpty()) {
                  tags.put(tag.name(), new Tag().name(tag.name()).description(tag.description()));
                }
            }
        }

        return tags;
    }

    private void handleSubResource(String[] apiConsumes, String httpMethod, String[] apiProduces, Map<String, Tag> tags, Method method, String operationPath, Operation operation) {
        if (isSubResource(httpMethod, method)) {
            Class<?> responseClass = method.getReturnType();
            read(responseClass, operationPath, httpMethod, true, apiConsumes, apiProduces, tags, operation.getParameters());
        }
    }

    protected boolean isSubResource(String httpMethod, Method method) {
        Class<?> responseClass = method.getReturnType();
        return (responseClass != null) && (httpMethod == null) && (AnnotationUtils.findAnnotation(method, Path.class) != null);
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


    public Operation parseMethod(String httpMethod, Method method) {
        int responseCode = 200;
        Operation operation = new Operation();
        ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);

        String operationId = getOperationId(method, httpMethod);
        
        String responseContainer = null;

        Type responseClassType = null;
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

            Map<String, Object> customExtensions = BaseReaderUtils.parseExtensions(apiOperation.extensions());
            operation.setVendorExtensions(customExtensions);

            if (!apiOperation.response().equals(Void.class) && !apiOperation.response().equals(void.class)) {
                responseClassType = apiOperation.response();
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

        if (responseClassType == null) {
            // pick out response from method declaration
            LOGGER.debug("picking up response class from method " + method);
            responseClassType = method.getGenericReturnType();
        }
        boolean hasApiAnnotation = false;
        if (responseClassType instanceof Class) {
            hasApiAnnotation = AnnotationUtils.findAnnotation((Class) responseClassType, Api.class) != null;
        }
        if ((responseClassType != null)
                && !responseClassType.equals(Void.class)
                && !responseClassType.equals(void.class)
                && !responseClassType.equals(javax.ws.rs.core.Response.class)
                && !hasApiAnnotation
                && !isSubResource(httpMethod, method)) {
            if (isPrimitive(responseClassType)) {
                Property property = ModelConverters.getInstance().readAsProperty(responseClassType);
                if (property != null) {
                    Property responseProperty = RESPONSE_CONTAINER_CONVERTER.withResponseContainer(responseContainer, property);

                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                }
            } else if (!responseClassType.equals(Void.class) && !responseClassType.equals(void.class)) {
                Map<String, Model> models = ModelConverters.getInstance().read(responseClassType);
                if (models.isEmpty()) {
                    Property p = ModelConverters.getInstance().readAsProperty(responseClassType);
                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(p)
                            .headers(defaultResponseHeaders));
                }
                for (String key : models.keySet()) {
                    Property responseProperty = RESPONSE_CONTAINER_CONVERTER.withResponseContainer(responseContainer, new RefProperty().asDefault(key));


                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                    swagger.model(key, models.get(key));
                }
            }
            Map<String, Model> models = ModelConverters.getInstance().readAll(responseClassType);
            for (Map.Entry<String, Model> entry : models.entrySet()) {
                swagger.model(entry.getKey(), entry.getValue());
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

        // process parameters
        Class<?>[] parameterTypes = method.getParameterTypes();
        Type[] genericParameterTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = findParamAnnotations(method);

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

        processOperationDecorator(operation, method);

        return operation;
    }

    public static Annotation[][] findParamAnnotations(Method method) {
		Annotation[][] paramAnnotation = method.getParameterAnnotations();

		Method overriddenMethod = ReflectionUtils.getOverriddenMethod(method);
		while(overriddenMethod != null) {
			paramAnnotation = merge(overriddenMethod.getParameterAnnotations(), paramAnnotation);
			overriddenMethod = ReflectionUtils.getOverriddenMethod(overriddenMethod);
		}
		return paramAnnotation;
	}


    private static Annotation[][] merge(Annotation[][] overriddenMethodParamAnnotation,
			Annotation[][] currentParamAnnotations) {
    	Annotation[][] mergedAnnotations = new Annotation[overriddenMethodParamAnnotation.length][];

    	for(int i=0; i<overriddenMethodParamAnnotation.length; i++) {
    		mergedAnnotations[i] = merge(overriddenMethodParamAnnotation[i], currentParamAnnotations[i]);
    	}
		return mergedAnnotations;
	}

	private static Annotation[] merge(Annotation[] annotations,
			Annotation[] annotations2) {
		List<Annotation> mergedAnnotations = new ArrayList<Annotation>();
		mergedAnnotations.addAll(Arrays.asList(annotations));
		mergedAnnotations.addAll(Arrays.asList(annotations2));
		return mergedAnnotations.toArray(new Annotation[0]);
	}

	public String extractOperationMethod(ApiOperation apiOperation, Method method, Iterator<SwaggerExtension> chain) {
        if (apiOperation != null && !apiOperation.httpMethod().isEmpty()) {
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


}
