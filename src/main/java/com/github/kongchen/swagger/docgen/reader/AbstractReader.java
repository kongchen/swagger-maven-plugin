package com.github.kongchen.swagger.docgen.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.jaxrs.BeanParamInjectParamExtention;
import com.github.kongchen.swagger.docgen.jaxrs.JaxrsParameterExtension;
import com.github.kongchen.swagger.docgen.spring.SpringSwaggerExtension;
import io.swagger.annotations.*;
import io.swagger.converter.ModelConverters;
import io.swagger.jaxrs.ParameterProcessor;
import io.swagger.jaxrs.ext.SwaggerExtension;
import io.swagger.jaxrs.ext.SwaggerExtensions;
import io.swagger.jersey.SwaggerJerseyJaxrs;
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
import io.swagger.models.properties.StringProperty;
import io.swagger.models.RefModel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
import org.springframework.core.annotation.AnnotationUtils;

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
            extensions.add(new BeanParamInjectParamExtention());
            extensions.add(new SwaggerJerseyJaxrs());
            extensions.add(new JaxrsParameterExtension());
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

    protected Map<String, Property> parseResponseHeaders(ResponseHeader[] headers) {
        Map<String, Property> responseHeaders = null;
        if (headers != null && headers.length > 0) {
            for (ResponseHeader header : headers) {
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

    protected List<Parameter> getParameters(Type type, List<Annotation> annotations) {
        // look for path, query
        Iterator<SwaggerExtension> chain = SwaggerExtensions.chain();
        List<Parameter> parameters = null;

        LOG.info("getParameters for " + type.getClass().getName());
        Set<Type> typesToSkip = new HashSet<Type>();
        if (chain.hasNext()) {
            SwaggerExtension extension = chain.next();
            LOG.info("trying extension " + extension);
            parameters = extension.extractParameters(annotations, type, typesToSkip, chain);
        }

        if (parameters.size() > 0) {
            for (Parameter parameter : parameters) {

                ParameterProcessor.applyAnnotations(swagger, parameter, type, annotations);
            }
        } else {
            LOG.info("no parameter found, looking at body params");
            if (typesToSkip.contains(type) == false) {
                Parameter param = null;
                param = ParameterProcessor.applyAnnotations(swagger, null, type, annotations);
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
            responseClass = apiResponse.response();  
            Response response = new Response()
                    .description(apiResponse.message())
                    .headers(responseHeaders);
            
            if (responseClass == null || responseClass.equals(Void.class)) {
                if (operation.getResponses() != null && !operation.getResponses().isEmpty()) {
                    Response apiOperationResponse = operation.getResponses().get(String.valueOf(apiResponse.code()));
                    if (apiOperationResponse != null) {
                        response.setSchema(apiOperationResponse.getSchema());
                    }
                }
            }
            
            if (apiResponse.code() == 0)
                operation.defaultResponse(response);
            else
                operation.response(apiResponse.code(), response);
            
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

    public Parameter convertApiImplicitParamToSwaggerParameter(ApiImplicitParam apiImplicitParam) {

        ModelConverterHelper modelConverterHelper = new ModelConverterHelper(new ObjectMapper());

        String paramType = apiImplicitParam.paramType();
        Property property = modelConverterHelper.getPropertyFromTypeName(apiImplicitParam.dataType());
        
        if (property == null) {
            // If the property cannot be determined from the ApiImplicitParam datatype, default to string.
            property = new StringProperty();
        }
        
        // Check to see if the parameter is an enum type
        String[] enumValuesArray = apiImplicitParam.allowableValues().split(",");
        boolean isEnum = !apiImplicitParam.allowableValues().isEmpty() && enumValuesArray.length > 0;
        List<String> enumValues = new ArrayList<String>();
        if (isEnum) {
            enumValues = new ArrayList<String>(Arrays.asList(enumValuesArray));
        }
        
        if ("header".equalsIgnoreCase(paramType)) {
            HeaderParameter parameter = new HeaderParameter();
            parameter.setDefaultValue(apiImplicitParam.defaultValue());
            parameter.setDescription(apiImplicitParam.value());
            parameter.setName(apiImplicitParam.name());
            parameter.setRequired(apiImplicitParam.required());
            parameter.setType(property.getType());
            if (apiImplicitParam.allowMultiple()) {
                parameter.setCollectionFormat("multi");
                parameter.setItems(property);
            } else {
                parameter.setProperty(property);
            }
            if (isEnum) {
                parameter.setEnum(enumValues);
            }
            
            return parameter;

        } else if ("path".equalsIgnoreCase(paramType)) {
            PathParameter parameter = new PathParameter();
            parameter.setDefaultValue(apiImplicitParam.defaultValue());
            parameter.setDescription(apiImplicitParam.value());
            parameter.setName(apiImplicitParam.name());
            parameter.setType(apiImplicitParam.dataType());
            parameter.setRequired(apiImplicitParam.required());
            parameter.setProperty(property);
            if (isEnum) {
                parameter.setEnum(enumValues);
            }
            
            return parameter;

        } else if ("query".equalsIgnoreCase(paramType)) {
            QueryParameter parameter = new QueryParameter();
            parameter.setDefaultValue(apiImplicitParam.defaultValue());
            parameter.setDescription(apiImplicitParam.value());
            parameter.setName(apiImplicitParam.name());
            parameter.setRequired(apiImplicitParam.required());
            parameter.setType(property.getType());
            if (apiImplicitParam.allowMultiple()) {
                parameter.setCollectionFormat("multi");
                parameter.setItems(property);
            } else {
                parameter.setProperty(property);
            }
            if (isEnum) {
                parameter.setEnum(enumValues);
            }
            
            return parameter;

        } else if ("body".equalsIgnoreCase(paramType)) {
            BodyParameter parameter = new BodyParameter();
            parameter.setName(apiImplicitParam.name());
            parameter.setRequired(apiImplicitParam.required());
            parameter.setDescription(apiImplicitParam.value());
            
            Class<?> cls = null;
            try {
                cls = Class.forName(apiImplicitParam.dataType());
            } catch (ClassNotFoundException e) {
            }
            
            if (cls != null) {
                Map<String, Model> models = ModelConverters.getInstance().read(cls);
                for (String key : models.keySet()) {
                    swagger.model(key, models.get(key));
                }
                models = ModelConverters.getInstance().readAll(cls);
                for (String key : models.keySet()) {
                    swagger.model(key, models.get(key));
                }
                parameter.setSchema(new RefModel(cls.getSimpleName()));
            }

            return parameter;

        } else if ("form".equalsIgnoreCase(paramType)) {
            FormParameter parameter = new FormParameter();
            parameter.setDefaultValue(apiImplicitParam.defaultValue());
            parameter.setName(apiImplicitParam.name());
            parameter.setRequired(apiImplicitParam.required());
            parameter.setType(property.getType());
            if (apiImplicitParam.allowMultiple()) {
                parameter.setCollectionFormat("multi");
                parameter.setItems(property);
            } else {
                parameter.setProperty(property);
            }
            if (isEnum) {
                parameter.setEnum(enumValues);
            }
            
            return parameter;
            
        }
        
        return null;
    }

    protected List<Parameter> getParametersFromApiImplicitParams(Method method) {

        List<Parameter> parameters = new ArrayList<Parameter>();
        
        // Process @ApiImplicitParams
        Annotation paramsAnnotation = AnnotationUtils.getAnnotation(method, ApiImplicitParams.class);
        if (paramsAnnotation != null && (paramsAnnotation instanceof ApiImplicitParams)) {
            ApiImplicitParams apiImplicitParamsAnnotation = (ApiImplicitParams) paramsAnnotation;
            for (ApiImplicitParam apiImplicitParam : apiImplicitParamsAnnotation.value()) {
                Parameter convertedParameter = convertApiImplicitParamToSwaggerParameter(apiImplicitParam);
                if (convertedParameter != null) {
                    parameters.add(convertedParameter);
                }
            }
        }

        return parameters;
    }
}

