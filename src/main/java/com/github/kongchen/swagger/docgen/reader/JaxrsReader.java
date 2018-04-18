package com.github.kongchen.swagger.docgen.reader;

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

import org.apache.maven.plugin.logging.Log;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.github.kongchen.swagger.docgen.jaxrs.BeanParamInjectParamExtention;
import com.github.kongchen.swagger.docgen.jaxrs.JaxrsParameterExtension;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.jersey.SwaggerJerseyJaxrs;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.ReflectionUtils;

public class JaxrsReader extends AbstractReader implements ClassSwaggerReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaxrsReader.class);
    private static final ResponseContainerConverter RESPONSE_CONTAINER_CONVERTER = new ResponseContainerConverter();

  public JaxrsReader(final Swagger swagger, final Log LOG) {
        super(swagger, LOG);
    }

    /**
     * This is overridden and made public so that it can be called by
     * {@link BeanParamInjectParamExtention}.
     */
    @Override
    public List<Parameter> getParameters(final Type type, final List<Annotation> annotations, final Set<Type> typesToSkip) {
        return super.getParameters(type, annotations, typesToSkip);
    }

    @Override
    protected void updateExtensionChain() {
    	final List<SwaggerExtension> extensions = new ArrayList<SwaggerExtension>();
    	extensions.add(new BeanParamInjectParamExtention(this));
        extensions.add(new SwaggerJerseyJaxrs());
        extensions.add(new JaxrsParameterExtension());
    	SwaggerExtensions.setExtensions(extensions);
    }

    @Override
    public Swagger read(final Set<Class<?>> classes) {
        for (final Class<?> cls : classes) {
            read(cls);
        }
        return swagger;
    }

    public Swagger getSwagger() {
        return swagger;
    }

    public Swagger read(final Class<?> cls) {
        return read(cls, "", null, false, new String[0], new String[0], new HashMap<String, Tag>(), new ArrayList<Parameter>());
    }

    protected Swagger read(final Class<?> cls, final String parentPath, final String parentMethod, final boolean readHidden, final String[] parentConsumes, final String[] parentProduces, final Map<String, Tag> parentTags, final List<Parameter> parentParameters) {
        if (swagger == null) {
            swagger = new Swagger();
        }
        final Api api = AnnotationUtils.findAnnotation(cls, Api.class);
        final Path apiPath = AnnotationUtils.findAnnotation(cls, Path.class);

        // only read if allowing hidden apis OR api is not marked as hidden
        if (!canReadApi(readHidden, api)) {
            return swagger;
        }

        final Map<String, Tag> tags = updateTagsForApi(parentTags, api);
        final List<SecurityRequirement> securities = getSecurityRequirements(api);
        final Map<String, Tag> discoveredTags = scanClasspathForTags();

        // merge consumes, pro duces

        // look for method-level annotated properties

        // handle subresources by looking at return type

        // parse the method
        final List<Method> filteredMethods = getFilteredMethods(cls);
        for (final Method method : filteredMethods) {
            final ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);
            if (apiOperation != null && apiOperation.hidden()) {
                continue;
            }
            Path methodPath = AnnotationUtils.findAnnotation(method, Path.class);

            String parentPathValue = String.valueOf(parentPath);
            //is method default handler within a subresource
            if(apiPath == null && methodPath == null && parentPath != null && readHidden){
                final String updatedMethodPath = String.valueOf(parentPath);
                final Path path = new Path(){
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
                final Map<String, String> regexMap = new HashMap<String, String>();
                operationPath = parseOperationPath(operationPath, regexMap);

                String httpMethod = extractOperationMethod(apiOperation, method, SwaggerExtensions.chain());

                final Operation operation = parseMethod(httpMethod, method);
                updateOperationParameters(parentParameters, regexMap, operation);
                updateOperationProtocols(apiOperation, operation);

                String[] apiConsumes = new String[0];
                String[] apiProduces = new String[0];

                final Consumes consumes = AnnotationUtils.findAnnotation(cls, Consumes.class);
                if (consumes != null) {
                    apiConsumes = consumes.value();
                }
                final Produces produces = AnnotationUtils.findAnnotation(cls, Produces.class);
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

    private List<Method> getFilteredMethods(final Class<?> cls) {
        final Method[] methods = cls.getMethods();
        final List<Method> filteredMethods = new ArrayList<Method>();
        for (final Method method : methods) {
            if (!method.isBridge()) {
                filteredMethods.add(method);
            }
        }
        return filteredMethods;
    }

    private void updateTagDescriptions(final Map<String, Tag> discoveredTags) {
        if (swagger.getTags() != null) {
            for (final Tag tag : swagger.getTags()) {
                final Tag rightTag = discoveredTags.get(tag.getName());
                if (rightTag != null && rightTag.getDescription() != null) {
                    tag.setDescription(rightTag.getDescription());
                }
            }
        }
    }

    private Map<String, Tag> scanClasspathForTags() {
        final Map<String, Tag> tags = new HashMap<String, Tag>();
        for (final Class<?> aClass: new Reflections("").getTypesAnnotatedWith(SwaggerDefinition.class)) {
            final SwaggerDefinition swaggerDefinition = AnnotationUtils.findAnnotation(aClass, SwaggerDefinition.class);

            for (final io.swagger.annotations.Tag tag : swaggerDefinition.tags()) {

                final String tagName = tag.name();
                if (!tagName.isEmpty()) {
                  tags.put(tag.name(), new Tag().name(tag.name()).description(tag.description()));
                }
            }
        }

        return tags;
    }

    private void handleSubResource(final String[] apiConsumes, final String httpMethod, final String[] apiProduces, final Map<String, Tag> tags, final Method method, final String operationPath, final Operation operation) {
        if (isSubResource(httpMethod, method)) {
            final Class<?> responseClass = method.getReturnType();
            read(responseClass, operationPath, httpMethod, true, apiConsumes, apiProduces, tags, operation.getParameters());
        }
    }

    protected boolean isSubResource(final String httpMethod, final Method method) {
        final Class<?> responseClass = method.getReturnType();
        return (responseClass != null) && (httpMethod == null) && (AnnotationUtils.findAnnotation(method, Path.class) != null);
    }

    private String getPath(final Path classLevelPath, final Path methodLevelPath, String parentPath) {
        if (classLevelPath == null && methodLevelPath == null) {
            return null;
        }
        final StringBuilder stringBuilder = new StringBuilder();
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


    public Operation parseMethod(final String httpMethod, final Method method) {
        final int responseCode = 200;
        final Operation operation = new Operation();
        final ApiOperation apiOperation = AnnotationUtils.findAnnotation(method, ApiOperation.class);

        String operationId = method.getName();
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

            final Set<Map<String, Object>> customExtensions = parseCustomExtensions(apiOperation.extensions());
            if (customExtensions != null) {
                for (final Map<String, Object> extension : customExtensions) {
                    if (extension == null) {
                        continue;
                    }
                    for (final Map.Entry<String, Object> map : extension.entrySet()) {
                        operation.setVendorExtension(map.getKey().startsWith("x-") ? map.getKey() : "x-" + map.getKey(), map.getValue());
                    }
                }
            }

            if (!apiOperation.response().equals(Void.class) && !apiOperation.response().equals(void.class)) {
                responseClassType = apiOperation.response();
            }
            if (!apiOperation.responseContainer().isEmpty()) {
                responseContainer = apiOperation.responseContainer();
            }
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
                final Property property = ModelConverters.getInstance().readAsProperty(responseClassType);
                if (property != null) {
                    final Property responseProperty = RESPONSE_CONTAINER_CONVERTER.withResponseContainer(responseContainer, property);

                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(responseProperty)
                            .headers(defaultResponseHeaders));
                }
            } else if (!responseClassType.equals(Void.class) && !responseClassType.equals(void.class)) {
                final Map<String, Model> models = ModelConverters.getInstance().read(responseClassType);
                if (models.isEmpty()) {
                    final Property p = ModelConverters.getInstance().readAsProperty(responseClassType);
                    operation.response(responseCode, new Response()
                            .description("successful operation")
                            .schema(p)
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
            final Map<String, Model> models = ModelConverters.getInstance().readAll(responseClassType);
            for (final Map.Entry<String, Model> entry : models.entrySet()) {
                swagger.model(entry.getKey(), entry.getValue());
            }
        }

        final Consumes consumes = AnnotationUtils.findAnnotation(method, Consumes.class);
        if (consumes != null) {
            for (final String mediaType : consumes.value()) {
                operation.consumes(mediaType);
            }
        }

        final Produces produces = AnnotationUtils.findAnnotation(method, Produces.class);
        if (produces != null) {
            for (final String mediaType : produces.value()) {
                operation.produces(mediaType);
            }
        }

        final ApiResponses responseAnnotation = AnnotationUtils.findAnnotation(method, ApiResponses.class);
        if (responseAnnotation != null) {
            updateApiResponse(operation, responseAnnotation);
        }

        if (AnnotationUtils.findAnnotation(method, Deprecated.class) != null) {
            operation.deprecated(true);
        }

        // process parameters
        final Class<?>[] parameterTypes = method.getParameterTypes();
        final Type[] genericParameterTypes = method.getGenericParameterTypes();
        final Annotation[][] paramAnnotations = findParamAnnotations(method);

        for (int i = 0; i < parameterTypes.length; i++) {
            final Type type = genericParameterTypes[i];
            final List<Annotation> annotations = Arrays.asList(paramAnnotations[i]);
            final List<Parameter> parameters = getParameters(type, annotations);

            for (final Parameter parameter : parameters) {
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

	public static Annotation[][] findParamAnnotations(final Method method) {
		Annotation[][] paramAnnotation = method.getParameterAnnotations();

		Method overriddenMethod = ReflectionUtils.getOverriddenMethod(method);
		while(overriddenMethod != null) {
			paramAnnotation = merge(overriddenMethod.getParameterAnnotations(), paramAnnotation);
			overriddenMethod = ReflectionUtils.getOverriddenMethod(overriddenMethod);
		}
		return paramAnnotation;
	}


    private static Annotation[][] merge(final Annotation[][] overriddenMethodParamAnnotation,
                                        final Annotation[][] currentParamAnnotations) {
    	final Annotation[][] mergedAnnotations = new Annotation[overriddenMethodParamAnnotation.length][];

    	for(int i=0; i<overriddenMethodParamAnnotation.length; i++) {
    		mergedAnnotations[i] = merge(overriddenMethodParamAnnotation[i], currentParamAnnotations[i]);
    	}
		return mergedAnnotations;
	}

	private static Annotation[] merge(final Annotation[] annotations,
                                      final Annotation[] annotations2) {
		final List<Annotation> mergedAnnotations = new ArrayList<Annotation>();
		mergedAnnotations.addAll(Arrays.asList(annotations));
		mergedAnnotations.addAll(Arrays.asList(annotations2));
		return mergedAnnotations.toArray(new Annotation[0]);
	}

	public String extractOperationMethod(final ApiOperation apiOperation, final Method method, final Iterator<SwaggerExtension> chain) {
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
            for (final Annotation declaredAnnotation : method.getDeclaredAnnotations()) {
                final Annotation[] innerAnnotations = declaredAnnotation.annotationType().getAnnotations();
                for (final Annotation innerAnnotation : innerAnnotations) {
                    if (innerAnnotation instanceof HttpMethod) {
                        final HttpMethod httpMethod = (HttpMethod) innerAnnotation;
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
