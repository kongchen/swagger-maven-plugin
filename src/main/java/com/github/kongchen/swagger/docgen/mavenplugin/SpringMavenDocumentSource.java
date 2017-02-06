package com.github.kongchen.swagger.docgen.mavenplugin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.util.MavenLogAdapter;
import com.github.kongchen.swagger.docgen.spring.SpringMvcApiReader;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import com.github.kongchen.swagger.docgen.util.AuthorizationUtils;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.Authorization;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.converter.SwaggerSchemaConverter;
import com.wordnik.swagger.core.SwaggerSpec;
import com.wordnik.swagger.model.*;
import org.apache.maven.plugin.logging.Log;
import org.springframework.web.bind.annotation.RequestMapping;
import scala.None;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author tedleman
 *         01/21/15
 * @author chekong
 * 05/13/2013
 */
public class SpringMavenDocumentSource extends AbstractDocumentSource {
    private final ApiSource apiSource;
    private SpringMvcApiReader reader;

    public OverrideConverter getOverriderConverter() {
        return overriderConverter;
    }

    private OverrideConverter overriderConverter;



    public SpringMavenDocumentSource(ApiSource apiSource, Log log) {
        super(new MavenLogAdapter(log), apiSource.getOutputPath(), apiSource.getOutputTemplate(),
                apiSource.getSwaggerDirectory(), apiSource.mustacheFileRoot, apiSource.isUseOutputFlatStructure(),
                apiSource.getOverridingModels(), apiSource.getApiSortComparator(), apiSource.getSwaggerSchemaConverter());

        setApiVersion(apiSource.getApiVersion());
        setBasePath(apiSource.getBasePath());
        setApiInfo(apiSource.getApiInfo());
        this.apiSource = apiSource;
    }

    @Override
    public void loadOverridingModels() throws GenerateException {
        OverrideConverter converter = new OverrideConverter();
        if (overridingModels != null) {
            try {
                JsonNode readTree = mapper.readTree(this.getClass()
                        .getResourceAsStream(overridingModels));

                for (JsonNode jsonNode : readTree) {
                    JsonNode classNameNode = jsonNode.get("className");
                    String className = classNameNode.asText();
                    JsonNode jsonStringNode = jsonNode.get("jsonString");
                    String jsonString = jsonStringNode.asText();

                    converter.add(className, jsonString);
                }
                this.overriderConverter = converter;
            } catch (JsonProcessingException e) {
                throw new GenerateException(
                        String.format(
                                "Swagger-overridingModels[%s] must be a valid JSON file!",
                                overridingModels), e);
            } catch (IOException e) {
                throw new GenerateException(String.format(
                        "Swagger-overridingModels[%s] not found!",
                        overridingModels), e);
            }
        }
        this.overriderConverter = converter;
    }

    @Override
    public void loadDocuments() throws GenerateException {
        reader = new SpringMvcApiReader(apiSource, this.overriderConverter, LOG);
        Map<String, SpringResource> resourceMap = new HashMap<String, SpringResource>();
        SwaggerConfig swaggerConfig = new SwaggerConfig();
        swaggerConfig.setApiVersion(apiSource.getApiVersion());
        swaggerConfig.setSwaggerVersion(SwaggerSpec.version());
        swaggerConfig.setApiInfo(toSwaggerApiInfo(apiSource.getApiInfo())); //TODO: can we pull this from spring?
        List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();
        List<AuthorizationType> authorizationTypes = new ArrayList<AuthorizationType>();

        if (apiSource.getSwaggerSchemaConverter() != null) {
            ModelConverterRegistry.registerConverter(apiSource.getSwaggerSchemaConverter());
        }

        //relate all methods to one base request mapping if multiple controllers exist for that mapping
        //get all methods from each controller & find their request mapping
        //create map - resource string (after first slash) as key, new SpringResource as value
        for (Class<?> c : apiSource.getValidClasses()) {
            //This try/catch block is to stop a bamboo build from failing due to NoClassDefFoundError
            //This occurs when a class or method loaded by reflections contains a type that has no dependency
            try {
                if (isSpringMvcController(c)) {
                    analyzeSpringController(resourceMap, c);
                    AuthorizationUtils.mergeAuthorizationTypes(authorizationTypes, getAuthorizationTypes(c));
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
        for (String str : resourceMap.keySet()) {

            SpringResource resource = resourceMap.get(str);

            try {
                ApiListing doc = getDocFromSpringResource(resource, swaggerConfig);
                if (doc != null) {
                    ApiListingReference apiListingReference = new ApiListingReference(doc.resourcePath(), doc.description(), doc.position());
                    apiListingReferences.add(apiListingReference);
                    acceptDocument(doc);
                    setBasePath(doc.basePath());
                }
            } catch (Exception e) {
                LOG.error("DOC NOT GENERATED FOR: " + resource.getResourceName(), e);
            }
        }
        // sort apiListingRefernce by position
        Collections.sort(apiListingReferences, new ApiListingReferenceComparator());

        serviceDocument = new ResourceListing(swaggerConfig.apiVersion(), swaggerConfig.swaggerVersion(),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiListingReferences.iterator())),
                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizationTypes.iterator())),
                swaggerConfig.info());
    }

    private static boolean isSpringMvcController(Class<?> c) {
        RequestMapping requestMapping = c.getAnnotation(RequestMapping.class);
        return requestMapping != null && requestMapping.value().length != 0;
    }

    private void analyzeSpringController(Map<String, SpringResource> resourceMap, Class<?> c) throws ClassNotFoundException {
        resourceMap = analyzeController(c, resourceMap);
        for (Method m : getAllMethods(c)) {
            if (m.isAnnotationPresent(RequestMapping.class)) {
                RequestMapping methodReq = m.getAnnotation(RequestMapping.class);
                //isolate resource name - attempt first by the first part of the mapping
                if (methodReq != null && methodReq.value().length != 0) {
                    for (int i = 0; i < methodReq.value().length; i++) {
                        String resourceName = Utils.parseResourceName(methodReq.value()[i]);
                        if (!(resourceName.equals(""))) {
                            String version = Utils.parseVersion(c.getAnnotation(RequestMapping.class).value()[0]);
                            //get version - first try by class mapping, then method
                            if (version.equals("")) {
                                //class mapping failed - use method
                                version = Utils.parseVersion(methodReq.value()[i]);
                            }
                            String resourceKey = Utils.createResourceKey(resourceName, version);
                            if ((!(resourceMap.containsKey(resourceKey)))) {
                                resourceMap.put(resourceKey, new SpringResource(c, resourceName, resourceKey, getDescription(c)));
                            }
                            resourceMap.get(resourceKey).addMethod(m);
                        }
                    }
                }
            }
        }
    }

    private static String getDescription(Class<?> c) {
        String description = "";
        if (c.isAnnotationPresent(Api.class)) {
            description = c.getAnnotation(Api.class).value();
        }
        return description;
    }

    private static List<AuthorizationType> getAuthorizationTypes(Class<?> c) {
        if (c.isAnnotationPresent(Api.class)) {
            Authorization[] authorizations = c.getAnnotation(Api.class).authorizations();
            if(authorizations != null && authorizations.length > 0) {
                return AuthorizationUtils.convertToAuthorizationTypes(authorizations);
            }
        }
        return Collections.emptyList();
    }

    private static List<Method> getAllMethods(Class<?> c) {
        List<Method> mList = new ArrayList<Method>(Arrays.asList(c.getMethods()));
        if (c.getSuperclass() != null) {
            mList.addAll(getAllMethods(c.getSuperclass()));
        }
        return mList;
    }

    private ApiInfo toSwaggerApiInfo(ApiSourceInfo info) {
        if (info == null) return null;
        return new ApiInfo(info.getTitle(), info.getDescription(),
                info.getTermsOfServiceUrl(), info.getContact(),
                info.getLicense(), info.getLicenseUrl());
    }

    private ApiListing getDocFromSpringResource(SpringResource res, SwaggerConfig swaggerConfig) throws Exception {
        ApiListing apiListing = reader.read(res, swaggerConfig);
        if (None.canEqual(apiListing)) return null;
        return apiListing;
    }

    //Helper method for loadDocuments()
    private Map<String, SpringResource> analyzeController(Class<?> clazz, Map<String, SpringResource> resourceMap) throws ClassNotFoundException {

        RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
        for (int i = 0; i < requestMapping.value().length; i++) {
            String controllerMapping = requestMapping.value()[i];
            String resourceName = Utils.parseResourceName(clazz);
            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping methodReq = m.getAnnotation(RequestMapping.class);
                    if (methodReq.value() == null || methodReq.value().length == 0 || Utils.parseResourceName(methodReq.value()[0]).equals("")) {
                        if (resourceName.length() != 0) {
                            String resourceKey = Utils.createResourceKey(resourceName, Utils.parseVersion(controllerMapping));
                            if ((!(resourceMap.containsKey(resourceKey)))) {
                                resourceMap.put(resourceKey, new SpringResource(clazz, resourceName, resourceKey, getDescription(clazz)));
                            }
                            resourceMap.get(resourceKey).addMethod(m);
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
}
