package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.jaxrs.BeanParamExtention;
import com.github.kongchen.swagger.docgen.spring.SpringSwaggerExtension;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.annotations.Authorization;
import com.wordnik.swagger.annotations.AuthorizationScope;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.jaxrs.DefaultParameterExtension;
import com.wordnik.swagger.jaxrs.ParameterProcessor;
import com.wordnik.swagger.jaxrs.ext.SwaggerExtension;
import com.wordnik.swagger.jaxrs.ext.SwaggerExtensions;
import com.wordnik.swagger.jaxrs.utils.ParameterUtils;
import com.wordnik.swagger.jersey.SwaggerJerseyJaxrs;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Response;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.SecurityRequirement;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.Tag;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.MapProperty;
import com.wordnik.swagger.models.properties.Property;
import com.wordnik.swagger.models.properties.RefProperty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by chekong on 15/4/28.
 */
public abstract class AbstractReader {
    protected final LogAdapter LOG;
    protected Swagger swagger;

    public AbstractReader(Swagger swagger, LogAdapter LOG) {
        this.swagger = swagger;
        this.LOG = LOG;
        updateExtensionChain();
    }

    private void updateExtensionChain() {
        List<SwaggerExtension> extensions = new ArrayList<SwaggerExtension>();
        if (this.getClass() == SpringMvcApiReader.class) {
            extensions.add(new SpringSwaggerExtension());
        } else {
            extensions.add(new BeanParamExtention());
            extensions.add(new SwaggerJerseyJaxrs());
            extensions.add(new DefaultParameterExtension());
        }
        SwaggerExtensions.setExtensions(extensions);
    }

    protected List<SecurityRequirement> getSecurityRequirements(Api api) {
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

    protected String parseOperationPath(String operationPath, Map<String, String> regexMap) {
        String[] pps = operationPath.split("/");
        String[] pathParts = new String[pps.length];


        for (int i = 0; i < pps.length; i++) {
            String p = pps[i];
            if (p.startsWith("{")) {
                int pos = p.indexOf(":");
                if (pos > 0) {
                    String left = p.substring(1, pos);
                    String right = p.substring(pos + 1, p.length() - 1);
                    pathParts[i] = "{" + left.trim() + "}";
                    regexMap.put(left.trim(), right);
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

    protected void updateOperationParameters(List<Parameter> parentParameters, Map<String, String> regexMap, Operation operation) {
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

    protected Map<String, Property> parseResponseHeaders(com.wordnik.swagger.annotations.ResponseHeader[] headers) {
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


    protected void updatePath(String operationPath, String httpMethod, Operation operation) {
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

    protected void updateTagsForOperation(Operation operation, ApiOperation op) {
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

    protected boolean canReadApi(boolean readHidden, Api api) {
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

    protected void updateOperationProtocols(ApiOperation apiOperation, Operation operation) {
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

    protected Map<String, Tag> updateTagsForApi(Map<String, Tag> parentTags, Api api) {
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

    protected void updateOperation(String[] apiConsumes, String[] apiProduces, Map<String, Tag> tags, List<SecurityRequirement> securities, Operation operation) {
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

    protected List<Parameter> getParameters(Class<?> cls, Type type, Annotation[] annotations) {
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
                //fix parameter type issue, try to access parameter's type
                try {
                    Field t = parameter.getClass().getDeclaredField("type");
                    t.setAccessible(true);
                    Object tval = t.get(parameter);
                    if (tval.equals("ref")) {
                        //fix to string
                        t.set(parameter, "string");
                    }
                    t.setAccessible(false);
                } catch (NoSuchFieldException e) {
                    //ignore
                } catch (IllegalAccessException e) {
                    //ignore
                }

                ParameterProcessor.applyAnnotations(swagger, parameter, cls, annotations, isArray);
            }
        } else {
            LOG.info("no parameter found, looking at body params");
            if (classesToSkip.contains(cls) == false) {
                Parameter param = null;
                if (type instanceof ParameterizedType) {
                    ParameterizedType ti = (ParameterizedType) type;
                    Type innerType = ti.getActualTypeArguments()[0];
                    if (innerType instanceof Class) {
                        param = ParameterProcessor.applyAnnotations(swagger, null, (Class) innerType, annotations, isArray);
                    }
                } else {
                    param = ParameterProcessor.applyAnnotations(swagger, null, cls, annotations, isArray);
                }
                if (param != null) {
                    for (Annotation annotation : annotations) {
                        if (annotation instanceof ApiParam) {
                            ApiParam apiParam = (ApiParam) annotation;
                            param.setRequired(apiParam.required());
                            break;
                        }
                    }
                    parameters.add(param);
                }


            }
        }
        return parameters;
    }

    protected void updateApiResponse(Operation operation, ApiResponses responseAnnotation) {
        Class<?> responseClass;
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
    }

    protected String[] updateOperationProduces(String[] parentProduces, String[] apiProduces, Operation operation) {

        if (parentProduces != null) {
            Set<String> both = new HashSet<String>(Arrays.asList(apiProduces));
            both.addAll(new HashSet<String>(Arrays.asList(parentProduces)));
            if (operation.getProduces() != null)
                both.addAll(new HashSet<String>(operation.getProduces()));
            apiProduces = both.toArray(new String[both.size()]);
        }
        return apiProduces;
    }

    protected String[] updateOperationConsumes(String[] parentConsumes, String[] apiConsumes, Operation operation) {

        if (parentConsumes != null) {
            Set<String> both = new HashSet<String>(Arrays.asList(apiConsumes));
            both.addAll(new HashSet<String>(Arrays.asList(parentConsumes)));
            if (operation.getConsumes() != null)
                both.addAll(new HashSet<String>(operation.getConsumes()));
            apiConsumes = both.toArray(new String[both.size()]);
        }
        return apiConsumes;
    }
}

