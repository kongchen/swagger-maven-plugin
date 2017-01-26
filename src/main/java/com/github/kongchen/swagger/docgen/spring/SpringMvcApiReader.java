package com.github.kongchen.swagger.docgen.spring;

import com.github.kongchen.swagger.docgen.util.LogAdapter;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.Authorization;
import com.wordnik.swagger.model.AuthorizationScope;
import com.wordnik.swagger.model.Model;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import scala.Option;
import scala.collection.JavaConversions;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.kongchen.swagger.docgen.spring.IgnoredClassParameterFilter.SPRING_IGNORED_PARAMETER_TYPES;
import static com.github.kongchen.swagger.docgen.spring.SpringReaderUtils.getGenericSubtype;

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
    private final ParameterGenerator parameterGenerator;
    private final ApiSource apiSource;
    private final ModelCollector modelCollector;
    private ApiListing apiListing;
    private String resourcePath;
    private List<String> produces;
    private List<String> consumes;
    private final ParameterFilter parameterFilter;
    private final LogAdapter logger;

    private static final Option<String> DEFAULT_OPTION = Option.empty(); //<--comply with scala option to prevent nulls

    public SpringMvcApiReader(ApiSource aSource, OverrideConverter overrideConverter, LogAdapter logger) {
        this(aSource, new IgnoredClassParameterFilter(SPRING_IGNORED_PARAMETER_TYPES), overrideConverter, logger);
    }

    public SpringMvcApiReader(ApiSource apiSource, ParameterFilter parameterFilter, OverrideConverter overrideConverter, LogAdapter logger) {
        this.apiSource = apiSource;
        apiListing = null;
        resourcePath = "";
        produces = new ArrayList<String>();
        consumes = new ArrayList<String>();
        this.logger = logger;
        this.parameterGenerator = new ParameterGenerator(logger);
        this.parameterFilter = parameterFilter;
        this.modelCollector = new ModelCollector(overrideConverter);
    }

    /**
     * @param basePath
     * @param c
     * @param methods            Run through annotations in approved controller class to generate ApiListing
     *                           This method is called from the document source class and calls the generating methods
     * @param overriderConverter
     * @param swaggerConfig
     */
    public ApiListing read(SpringResource resource, SwaggerConfig swaggerConfig) {
        List<Method> methods = resource.getMethods();
        List<String> protocols = new ArrayList<String>();
        List<ApiDescription> apiDescriptions = new ArrayList<ApiDescription>();
        List<Authorization> authorizations = new ArrayList<Authorization>();
        String newBasePath = apiSource.getBasePath();
        String description = null;
        int position = 0;

        // Add the description from the controller api
        Class<?> controller = resource.getControllerClass();
        if (controller != null && controller.isAnnotationPresent(Api.class)) {
            Api api = controller.getAnnotation(Api.class);
            description = api.description();
            position = api.position();
            if (api.authorizations() != null && api.authorizations().length > 0) {
                addAuthorization(authorizations, api.authorizations());
            }
        }

        resourcePath = resource.getControllerMapping();

        Map<String, List<Method>> apiMethodMap = new HashMap<String, List<Method>>();
        for (Method m : methods) {
            if (m.isAnnotationPresent(RequestMapping.class)) {
                String path = getMethodPath(m.getAnnotation(RequestMapping.class));
                if (apiMethodMap.containsKey(path)) {
                    apiMethodMap.get(path).add(m);
                } else {
                    List<Method> ms = new ArrayList<Method>();
                    ms.add(m);
                    apiMethodMap.put(path, ms);
                }
            }
        }
        for (String p : apiMethodMap.keySet()) {
            List<Operation> operations = new ArrayList<Operation>();
            for (Method m : apiMethodMap.get(p)) {
                operations.add(generateOperation(m));
            }
            //reorder operations
            Collections.sort(operations, new Comparator<Operation>() {
                @Override
                public int compare(Operation o1, Operation o2) {
                    return o1.position() - o2.position();
                }
            });

            apiDescriptions.add(new ApiDescription(p, DEFAULT_OPTION,
                    scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(operations.iterator())), false));


        }

        apiListing = new ApiListing(swaggerConfig.apiVersion(), swaggerConfig.getSwaggerVersion(), newBasePath, resourcePath,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(produces.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(consumes.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(protocols.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizations.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiDescriptions.iterator())),
                generateModels(modelCollector.getModels()), Option.apply(description), position);
        return apiListing;
    }

    private String getMethodPath(RequestMapping requestMapping) {
        if (requestMapping.value() != null && requestMapping.value().length != 0) {
            return generateFullPath(requestMapping.value()[0]);
        } else {
            return resourcePath;
        }
    }

    private void addAuthorization(List<Authorization> authorizations, com.wordnik.swagger.annotations.Authorization[] annotations) {
        for (com.wordnik.swagger.annotations.Authorization authorization : annotations) {
            List<AuthorizationScope> scopes = new ArrayList<AuthorizationScope>();
            for (com.wordnik.swagger.annotations.AuthorizationScope scope : authorization.scopes()) {
                scopes.add(new AuthorizationScope(scope.scope(), scope.description()));
            }
            authorizations.add(new Authorization(authorization.value(), scopes.toArray(new AuthorizationScope[scopes.size()])));
        }
    }

    //--------Swagger Resource Generators--------//

    private String generateFullPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            return this.resourcePath + (path.startsWith("/") ? path : '/' + path);
        } else {
            return this.resourcePath;
        }
    }

    /**
     * Generates operations for the ApiDescription
     *
     * @param Method m
     * @return Operation
     */
    private Operation generateOperation(Method m) {
        String method = null;
        String description = null;
        String notes = null;

        List<Authorization> authorizations = new ArrayList<Authorization>();

        RequestMapping requestMapping = m.getAnnotation(RequestMapping.class);
        ResponseBody responseBody = m.getAnnotation(ResponseBody.class);

        Class<?> containerClz = getReturnedType(m);
        Class<?> clazz = getGenericSubtype(m.getReturnType(), m.getGenericReturnType());

        List<String> opProduces = asListOrEmptyIfNull(requestMapping.produces());
        addWithoutDuplicates(produces, opProduces);
        List<String> opConsumes = asListOrEmptyIfNull(requestMapping.consumes());
        addWithoutDuplicates(consumes, opConsumes);


        ApiOperation apiOperation = m.getAnnotation(ApiOperation.class);
        if (apiOperation != null) {
            description = apiOperation.value();
            notes = apiOperation.notes();
            if (apiOperation.authorizations() != null && apiOperation.authorizations().length > 0) {
                addAuthorization(authorizations, apiOperation.authorizations());
            }
        }

        List<ResponseMessage> responseMessages = generateResponseMessages(m);

        String responseBodyName = "";
        if (responseBody != null) {
            if (!containerClz.equals(clazz)) {
                responseBodyName = containerClz.getSimpleName() + "[" + clazz.getSimpleName() + "]";
            } else {
                responseBodyName = (clazz.getSimpleName());
            }
            modelCollector.addModel(clazz);
        }
        if (requestMapping.method() != null && requestMapping.method().length != 0) {
            method = requestMapping.method()[0].toString();
        }

        List<Parameter> parameters = new ArrayList<Parameter>();
        if (m.getParameterTypes() != null) {
            parameters = generateParameters(m);
        }

        return new Operation(method,
                description, notes, responseBodyName, m.getName(), apiOperation.position(),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opProduces.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(opConsumes.iterator())),
                null,
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizations.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(parameters.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(responseMessages.iterator())),
                DEFAULT_OPTION);
    }

    private Class<?> getReturnedType(Method m) {
        if (m.getReturnType().equals(ResponseEntity.class)) {
            return getGenericSubtype(m.getReturnType(), m.getGenericReturnType());
        } else {
            return m.getReturnType();
        }
    }

    private static <T> List<T> asListOrEmptyIfNull(T[] array) {
        if (array == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(array);
        }
    }

    private static <T> void addWithoutDuplicates(Collection<T> targetCollection, Collection<T> elements) {
        for (T str : elements) {
            if (!targetCollection.contains(str)) {
                targetCollection.add(str);
            }
        }
    }

    /**
     * Generates parameters for each Operation
     *
     * @param Method m
     * @return List<Parameter>
     */
    private List<Parameter> generateParameters(Method m) {
        Annotation[][] annotations = m.getParameterAnnotations();
        List<Parameter> params = new ArrayList<Parameter>();
        for (int i = 0; i < annotations.length; i++) { //loops through parameters
            Class<?> parameterClass = m.getParameterTypes()[i];
            ParameterMetadata parameterMetadata = new ParameterMetadata(parameterClass, annotations[i]);
            if (isNotIgnoredParameter(parameterMetadata)) {
                modelCollector.addModel(parameterClass);
                Parameter parameter = parameterGenerator.generateParameter(parameterMetadata);
                params.add(parameter);
            } else {
                logger.info("Ignoring parameter " + parameterMetadata);
            }
        }
        return params;
    }

    private boolean isNotIgnoredParameter(ParameterMetadata parameterMetadata) {
        return !parameterFilter.isIgnoredParameter(parameterMetadata);
    }

    /**
     * Generates response messages for each Operation
     *
     * @param Method m
     * @return List<ResponseMessage>
     */
    private List<ResponseMessage> generateResponseMessages(Method m) {
        List<ResponseMessage> responseMessages = new ArrayList<ResponseMessage>();
        ApiResponses apiresponses = m.getAnnotation(ApiResponses.class);
        if (apiresponses != null) {
            for (ApiResponse apiResponse : apiresponses.value()) {
                if (apiResponse.response() == null || apiResponse.response().equals(java.lang.Void.class)) {
                    responseMessages.add(new ResponseMessage(apiResponse.code(), apiResponse.message(), Option.<String>empty()));
                } else {
                    modelCollector.addModel(apiResponse.response());
                    responseMessages.add(new ResponseMessage(apiResponse.code(), apiResponse.message(), Option.apply(apiResponse.response().getSimpleName())));
                }
            }
        } else {
            ResponseStatus responseStatus = m.getAnnotation(ResponseStatus.class);

            responseMessages.add(new ResponseMessage(responseStatus.value().value(), responseStatus.reason(), DEFAULT_OPTION));
        }
        return responseMessages;
    }

    private Option<scala.collection.immutable.Map<String, Model>> generateModels(Map<String, Model> javaModelMap) {
        return Option.apply(Utils.toScalaImmutableMap(javaModelMap));
    }


}
