package com.github.kongchen.swagger.docgen.reader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.BeanParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang3.reflect.TypeUtils;
import org.apache.maven.plugin.logging.Log;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import com.sun.jersey.api.core.InjectParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.AuthorizationScope;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Scheme;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.ParameterProcessor;
import io.swagger.util.PathUtils;

/**
 * @author chekong on 15/4/28.
 */
public abstract class AbstractReader {
    protected final Log LOG;
    protected Swagger swagger;
    private Set<Type> typesToSkip = new HashSet<Type>();

    public Set<Type> getTypesToSkip() {
        return typesToSkip;
    }

    public void setTypesToSkip(final List<Type> typesToSkip) {
        this.typesToSkip = new HashSet<Type>(typesToSkip);
    }

    public void setTypesToSkip(final Set<Type> typesToSkip) {
        this.typesToSkip = typesToSkip;
    }

    public void addTypeToSkippedTypes(final Type type) {
        this.typesToSkip.add(type);
    }

    public AbstractReader(final Swagger swagger, final Log LOG) {
        this.swagger = swagger;
        this.LOG = LOG;
        updateExtensionChain();
    }

    /**
     * Method which allows sub-classes to modify the Swagger extension chain.
     */
    protected void updateExtensionChain() {
    	// default implementation does nothing
    }

    protected List<SecurityRequirement> getSecurityRequirements(final Api api) {
        final List<SecurityRequirement> securities = new ArrayList<SecurityRequirement>();
        if(api == null) {
            return securities;
        }

        for (final Authorization auth : api.authorizations()) {
            if (auth.value().isEmpty()) {
                continue;
            }
            final SecurityRequirement security = new SecurityRequirement();
            security.setName(auth.value());
            for (final AuthorizationScope scope : auth.scopes()) {
                if (!scope.scope().isEmpty()) {
                    security.addScope(scope.scope());
                }
            }
            securities.add(security);
        }
        return securities;
    }

    protected String parseOperationPath(final String operationPath, final Map<String, String> regexMap) {
        return PathUtils.parsePath(operationPath, regexMap);
    }

    protected void updateOperationParameters(final List<Parameter> parentParameters, final Map<String, String> regexMap, final Operation operation) {
        if (parentParameters != null) {
            for (final Parameter param : parentParameters) {
                operation.parameter(param);
            }
        }
        for (final Parameter param : operation.getParameters()) {
            final String pattern = regexMap.get(param.getName());
            if (pattern != null) {
                param.setPattern(pattern);
            }
        }
    }

    protected Map<String, Property> parseResponseHeaders(final ResponseHeader[] headers) {
        if (headers == null) {
            return null;
        }
        Map<String, Property> responseHeaders = null;
        for (final ResponseHeader header : headers) {
            if (header.name().isEmpty()) {
                continue;
            }
            if (responseHeaders == null) {
                responseHeaders = new HashMap<String, Property>();
            }
            final Class<?> cls = header.response();

            if (!cls.equals(Void.class) && !cls.equals(void.class)) {
                final Property property = ModelConverters.getInstance().readAsProperty(cls);
                if (property != null) {
                    final Property responseProperty;

                    if (header.responseContainer().equalsIgnoreCase("list")) {
                        responseProperty = new ArrayProperty(property);
                    } else if (header.responseContainer().equalsIgnoreCase("map")) {
                        responseProperty = new MapProperty(property);
                    } else {
                        responseProperty = property;
                    }
                    responseProperty.setDescription(header.description());
                    responseHeaders.put(header.name(), responseProperty);
                }
            }
        }
        return responseHeaders;
    }

    protected Set<Map<String, Object>> parseCustomExtensions(final Extension[] extensions) {
        if (extensions == null) {
            return Collections.emptySet();
        }
        final Set<Map<String, Object>> resultSet = new HashSet<Map<String, Object>>();
        for (final Extension extension : extensions) {
            if (extension == null) {
                continue;
            }
            final Map<String, Object> extensionProperties = new HashMap<String, Object>();
            for (final ExtensionProperty extensionProperty : extension.properties()) {
                final String name = extensionProperty.name();
                if (!name.isEmpty()) {
                    final String value = extensionProperty.value();
                    extensionProperties.put(name, value);
                }
            }
            if (!extension.name().isEmpty()) {
                final Map<String, Object> wrapper = new HashMap<String, Object>();
                wrapper.put(extension.name(), extensionProperties);
                resultSet.add(wrapper);
            } else {
                resultSet.add(extensionProperties);
            }
        }
        return resultSet;
    }

    protected void updatePath(final String operationPath, final String httpMethod, final Operation operation) {
        if (httpMethod == null) {
            return;
        }
        Path path = swagger.getPath(operationPath);
        if (path == null) {
            path = new Path();
            swagger.path(operationPath, path);
        }
        path.set(httpMethod, operation);
    }

    protected void updateTagsForOperation(final Operation operation, final ApiOperation apiOperation) {
        if (apiOperation == null) {
            return;
        }
        for (final String tag : apiOperation.tags()) {
            if (!tag.isEmpty()) {
                operation.tag(tag);
                swagger.tag(new Tag().name(tag));
            }
        }
    }

    protected boolean canReadApi(final boolean readHidden, final Api api) {
        return (api == null) || (readHidden) || (!api.hidden());
    }

    protected Set<Tag> extractTags(final Api api) {
        final Set<Tag> output = new LinkedHashSet<Tag>();
        if(api == null) {
            return output;
        }

        boolean hasExplicitTags = false;
        for (final String tag : api.tags()) {
            if (!tag.isEmpty()) {
                hasExplicitTags = true;
                output.add(new Tag().name(tag));
            }
        }
        if (!hasExplicitTags) {
            // derive tag from api path + description
            final String tagString = api.value().replace("/", "");
            if (!tagString.isEmpty()) {
                final Tag tag = new Tag().name(tagString);
                if (!api.description().isEmpty()) {
                    tag.description(api.description());
                }
                output.add(tag);
            }
        }
        return output;
    }

    protected void updateOperationProtocols(final ApiOperation apiOperation, final Operation operation) {
        if(apiOperation == null) {
            return;
        }
        final String[] protocols = apiOperation.protocols().split(",");
        for (final String protocol : protocols) {
            final String trimmed = protocol.trim();
            if (!trimmed.isEmpty()) {
                operation.scheme(Scheme.forValue(trimmed));
            }
        }
    }

    protected Map<String, Tag> updateTagsForApi(final Map<String, Tag> parentTags, final Api api) {
        // the value will be used as a tag for 2.0 UNLESS a Tags annotation is present
        final Map<String, Tag> tagsMap = new HashMap<String, Tag>();
        for (final Tag tag : extractTags(api)) {
            tagsMap.put(tag.getName(), tag);
        }
        if (parentTags != null) {
            tagsMap.putAll(parentTags);
        }
        for (final Tag tag : tagsMap.values()) {
            swagger.tag(tag);
        }
        return tagsMap;
    }

    protected boolean isPrimitive(final Type cls) {
        boolean isPrimitive = false;

        final Property property = ModelConverters.getInstance().readAsProperty(cls);
        if (property == null) {
            isPrimitive = false;
        } else if ("integer".equals(property.getType())) {
            isPrimitive = true;
        } else if ("string".equals(property.getType())) {
            isPrimitive = true;
        } else if ("number".equals(property.getType())) {
            isPrimitive = true;
        } else if ("boolean".equals(property.getType())) {
            isPrimitive = true;
        } else if ("array".equals(property.getType())) {
            isPrimitive = true;
        } else if ("file".equals(property.getType())) {
            isPrimitive = true;
        }
        return isPrimitive;
    }

    protected void updateOperation(final String[] apiConsumes, final String[] apiProduces, final Map<String, Tag> tags, final List<SecurityRequirement> securities, final Operation operation) {
        if (operation == null) {
            return;
        }
        if (operation.getConsumes() == null) {
            for (final String mediaType : apiConsumes) {
                operation.consumes(mediaType);
            }
        }
        if (operation.getProduces() == null) {
            for (final String mediaType : apiProduces) {
                operation.produces(mediaType);
            }
        }

        if (operation.getTags() == null) {
            for (final String tagString : tags.keySet()) {
                operation.tag(tagString);
            }
        }
        for (final SecurityRequirement security : securities) {
            operation.security(security);
        }
    }

    private boolean isApiParamHidden(final List<Annotation> parameterAnnotations) {
        for (final Annotation parameterAnnotation : parameterAnnotations) {
            if (parameterAnnotation instanceof ApiParam) {
                return ((ApiParam) parameterAnnotation).hidden();
            }
        }

        return false;
    }

    private boolean hasValidAnnotations(final List<Annotation> parameterAnnotations) {
        // Because method parameters can contain parameters that are valid, but
        // not part of the API contract, first check to make sure the parameter
        // has at lease one annotation before processing it.  Also, check a
        // whitelist to make sure that the annotation of the parameter is
        // compatible with spring-maven-plugin

        final List<Type> validParameterAnnotations = new ArrayList<Type>();
        validParameterAnnotations.add(ModelAttribute.class);
        validParameterAnnotations.add(BeanParam.class);
        validParameterAnnotations.add(InjectParam.class);
        validParameterAnnotations.add(ApiParam.class);
        validParameterAnnotations.add(PathParam.class);
        validParameterAnnotations.add(QueryParam.class);
        validParameterAnnotations.add(HeaderParam.class);
        validParameterAnnotations.add(FormParam.class);
        validParameterAnnotations.add(RequestParam.class);
        validParameterAnnotations.add(RequestBody.class);
        validParameterAnnotations.add(PathVariable.class);
        validParameterAnnotations.add(RequestHeader.class);
        validParameterAnnotations.add(RequestPart.class);
        validParameterAnnotations.add(CookieValue.class);


        boolean hasValidAnnotation = false;
        for (final Annotation potentialAnnotation : parameterAnnotations) {
            if (validParameterAnnotations.contains(potentialAnnotation.annotationType())) {
                hasValidAnnotation = true;
                break;
            }
        }

        return hasValidAnnotation;
    }

    // this is final to enforce that only the implementation method below can be overridden, to avoid confusion
    protected final List<Parameter> getParameters(final Type type, final List<Annotation> annotations) {
        return getParameters(type, annotations, typesToSkip);
    }

    // this method exists so that outside callers can choose their own custom types to skip
    protected List<Parameter> getParameters(final Type type, final List<Annotation> annotations, final Set<Type> typesToSkip) {
        if (!hasValidAnnotations(annotations) || isApiParamHidden(annotations)) {
            return Collections.emptyList();
        }

        final Iterator<SwaggerExtension> chain = SwaggerExtensions.chain();
        List<Parameter> parameters = new ArrayList<Parameter>();
        final Class<?> cls = TypeUtils.getRawType(type, type);
        LOG.debug("Looking for path/query/header/form/cookie params in " + cls);

        if (chain.hasNext()) {
            final SwaggerExtension extension = chain.next();
            LOG.debug("trying extension " + extension);
            parameters = extension.extractParameters(annotations, type, typesToSkip, chain);
        }

        if (!parameters.isEmpty()) {
            for (final Parameter parameter : parameters) {
                ParameterProcessor.applyAnnotations(swagger, parameter, type, annotations);
            }
        } else {
            LOG.debug("Looking for body params in " + cls);
            if (!typesToSkip.contains(type)) {
                final Parameter param = ParameterProcessor.applyAnnotations(swagger, null, type, annotations);
                if (param != null) {
                    parameters.add(param);
                }
            }
        }
        return parameters;
    }

    protected void updateApiResponse(final Operation operation, final ApiResponses responseAnnotation) {
        for (final ApiResponse apiResponse : responseAnnotation.value()) {
            final Map<String, Property> responseHeaders = parseResponseHeaders(apiResponse.responseHeaders());
            final Class<?> responseClass = apiResponse.response();
            final Response response = new Response()
                    .description(apiResponse.message())
                    .headers(responseHeaders);

            if (responseClass.equals(Void.class)) {
                if (operation.getResponses() != null) {
                    final Response apiOperationResponse = operation.getResponses().get(String.valueOf(apiResponse.code()));
                    if (apiOperationResponse != null) {
                        response.setSchema(apiOperationResponse.getSchema());
                    }
                }
            } else {
                Map<String, Model> models = ModelConverters.getInstance().read(responseClass);
                for (final String key : models.keySet()) {
                    final Property schema = new RefProperty().asDefault(key);
                    if (apiResponse.responseContainer().equals("List")) {
                        response.schema(new ArrayProperty(schema));
                    } else {
                        response.schema(schema);
                    }
                    swagger.model(key, models.get(key));
                }
                models = ModelConverters.getInstance().readAll(responseClass);
                for (final Map.Entry<String, Model> entry : models.entrySet()) {
                    swagger.model(entry.getKey(), entry.getValue());
                }

                if (response.getSchema() == null) {
                    final Map<String, Response> responses = operation.getResponses();
                    if (responses != null) {
                        final Response apiOperationResponse = responses.get(String.valueOf(apiResponse.code()));
                        if (apiOperationResponse != null) {
                            response.setSchema(apiOperationResponse.getSchema());
                        }
                    }
                }
            }

            if (apiResponse.code() == 0) {
                operation.defaultResponse(response);
            } else {
                operation.response(apiResponse.code(), response);
            }
        }
    }

    protected String[] updateOperationProduces(final String[] parentProduces, String[] apiProduces, final Operation operation) {
        if (parentProduces != null) {
            final Set<String> both = new LinkedHashSet<String>(Arrays.asList(apiProduces));
            both.addAll(Arrays.asList(parentProduces));
            if (operation.getProduces() != null) {
                both.addAll(operation.getProduces());
            }
            apiProduces = both.toArray(new String[both.size()]);
        }
        return apiProduces;
    }

    protected String[] updateOperationConsumes(final String[] parentConsumes, String[] apiConsumes, final Operation operation) {
        if (parentConsumes != null) {
            final Set<String> both = new LinkedHashSet<String>(Arrays.asList(apiConsumes));
            both.addAll(Arrays.asList(parentConsumes));
            if (operation.getConsumes() != null) {
                both.addAll(operation.getConsumes());
            }
            apiConsumes = both.toArray(new String[both.size()]);
        }
        return apiConsumes;
    }

    protected void readImplicitParameters(final Method method, final Operation operation) {
        final ApiImplicitParams implicitParams = AnnotationUtils.findAnnotation(method, ApiImplicitParams.class);
        if (implicitParams == null) {
            return;
        }
        for (final ApiImplicitParam param : implicitParams.value()) {
            Class<?> cls;
            try {
                cls = Class.forName(param.dataType());
            } catch (final ClassNotFoundException e) {
                cls = method.getDeclaringClass();
            }

            final Parameter p = readImplicitParam(param, cls);
            if (p != null) {
                operation.addParameter(p);
            }
        }
    }

    protected Parameter readImplicitParam(final ApiImplicitParam param, final Class<?> apiClass) {
        final Parameter parameter;
        if (param.paramType().equalsIgnoreCase("path")) {
            parameter = new PathParameter();
        } else if (param.paramType().equalsIgnoreCase("query")) {
            parameter = new QueryParameter();
        } else if (param.paramType().equalsIgnoreCase("form") || param.paramType().equalsIgnoreCase("formData")) {
            parameter = new FormParameter();
        } else if (param.paramType().equalsIgnoreCase("body")) {
            parameter = new BodyParameter();
        } else if (param.paramType().equalsIgnoreCase("header")) {
            parameter = new HeaderParameter();
        } else {
            return null;
        }

        return ParameterProcessor.applyAnnotations(swagger, parameter, apiClass, Arrays.asList(new Annotation[]{param}));
    }

    void processOperationDecorator(final Operation operation, final Method method) {
        final Iterator<SwaggerExtension> chain = SwaggerExtensions.chain();
        if (chain.hasNext()) {
            final SwaggerExtension extension = chain.next();
            extension.decorateOperation(operation, method, chain);
        }
    }

}

