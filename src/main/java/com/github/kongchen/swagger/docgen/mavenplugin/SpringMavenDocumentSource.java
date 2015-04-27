package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.spring.SpringMvcApiReader;
import com.github.kongchen.swagger.docgen.spring.SpringResource;
import com.github.kongchen.swagger.docgen.util.SprintUtils;
import com.wordnik.swagger.core.filter.SpecFilter;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import org.apache.maven.plugin.logging.Log;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tedleman
 *         01/21/15
 * @author: chekong
 * 05/13/2013
 */
public class SpringMavenDocumentSource extends AbstractDocumentSource {

    private final SpecFilter specFilter = new SpecFilter();

    public SpringMavenDocumentSource(ApiSource apiSource, Log log) {
        super( new LogAdapter(log),apiSource);

    }

    @Override
    public void loadDocuments() throws GenerateException {
        Map<String, SpringResource> resourceMap = new HashMap<String, SpringResource>();

        //relate all methods to one base request mapping if multiple controllers exist for that mapping
        //get all methods from each controller & find their request mapping
        //create map - resource string (after first slash) as key, new SpringResource as value
        resourceMap = generateResourceMap(resourceMap);
        for (String str : resourceMap.keySet()) {
            Path doc = null;
            SpringResource resource = resourceMap.get(str);

            try {
                getDocFromSpringResource(resource, swagger);

            } catch (Exception e) {
                LOG.error("DOC NOT GENERATED FOR: " + resource.getResourceName());
                e.printStackTrace();
            }
            if (doc == null) continue;
//            ApiListingReference apiListingReference = new ApiListingReference(doc.resourcePath(), doc.description(), doc.position());
//            apiListingReferences.add(apiListingReference);
//            acceptDocument(doc);

        }
        // sort apiListingRefernce by position
//        Collections.sort(apiListingReferences, new Comparator<ApiListingReference>() {
//            @Override
//            public int compare(ApiListingReference o1, ApiListingReference o2) {
//                if (o1 == null && o2 == null) return 0;
//                if (o1 == null && o2 != null) return -1;
//                if (o1 != null && o2 == null) return 1;
//                return o1.position() - o2.position();
//            }
//        });
//
//        serviceDocument = new ResourceListing(swaggerConfig.apiVersion(), swaggerConfig.swaggerVersion(),
//                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(apiListingReferences.iterator())),
//                scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(authorizationTypes.iterator())),
//                swaggerConfig.info());
    }

    private Map<String, SpringResource> generateResourceMap(Map<String, SpringResource> resourceMap) throws GenerateException {
        for (Class<?> c : apiSource.getValidClasses()) {
            RequestMapping requestMapping = c.getAnnotation(RequestMapping.class);
            String description = "";
            if (requestMapping != null && requestMapping.value().length != 0) {
                //This try/catch block is to stop a bamboo build from failing due to NoClassDefFoundError
                //This occurs when a class or method loaded by reflections contains a type that has no dependency
                try {
                    resourceMap = analyzeController(c, resourceMap, description);
                    List<Method> mList = new ArrayList<Method>(Arrays.asList(c.getMethods()));
                    if (c.getSuperclass() != null) {
                        mList.addAll(Arrays.asList(c.getSuperclass().getMethods()));
                    }
                    for (Method m : mList) {
                        if (m.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping methodReq = m.getAnnotation(RequestMapping.class);
                            //isolate resource name - attempt first by the first part of the mapping
                            if (methodReq != null && methodReq.value().length != 0) {
                                for (int i = 0; i < methodReq.value().length; i++) {
                                    String resourceKey = "";
                                    String resourceName = SprintUtils.parseResourceName(methodReq.value()[i]);
                                    if (!(resourceName.equals(""))) {
                                        String version = SprintUtils.parseVersion(requestMapping.value()[0]);
                                        //get version - first try by class mapping, then method
                                        if (version.equals("")) {
                                            //class mapping failed - use method
                                            version = SprintUtils.parseVersion(methodReq.value()[i]);
                                        }
                                        resourceKey = SprintUtils.createResourceKey(resourceName, version);
                                        if ((!(resourceMap.containsKey(resourceKey)))) {
                                            resourceMap.put(resourceKey, new SpringResource(c, resourceName, resourceKey, description));
                                        }
                                        resourceMap.get(resourceKey).addMethod(m);
                                    }
                                }
                            }
                        }
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
        }
        return resourceMap;
    }

//    private ApiInfo toSwaggerApiInfo(ApiSourceInfo info) {
//        if (info == null) return null;
//        return new ApiInfo(info.getTitle(), info.getDescription(),
//                info.getTermsOfServiceUrl(), info.getContact(),
//                info.getLicense(), info.getLicenseUrl());
//    }

    private void getDocFromSpringResource(SpringResource res, Swagger swagger) throws Exception {
        SpringMvcApiReader reader = new SpringMvcApiReader(apiSource, swagger, LOG);
        reader.read(res);
    }

    //Helper method for loadDocuments()
    private Map<String, SpringResource> analyzeController(Class<?> clazz, Map<String, SpringResource> resourceMap,
                                                          String description) throws ClassNotFoundException {

        for (int i = 0; i < clazz.getAnnotation(RequestMapping.class).value().length; i++) {
            String controllerMapping = clazz.getAnnotation(RequestMapping.class).value()[i];
            String resourceName = SprintUtils.parseResourceName(clazz);
            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping methodReq = m.getAnnotation(RequestMapping.class);
                    if (methodReq.value() == null || methodReq.value().length == 0 || SprintUtils.parseResourceName(methodReq.value()[0]).equals("")) {
                        if (resourceName.length() != 0) {
                            String resourceKey = SprintUtils.createResourceKey(resourceName, SprintUtils.parseVersion(controllerMapping));
                            if ((!(resourceMap.containsKey(resourceKey)))) {
                                resourceMap.put(resourceKey, new SpringResource(clazz, resourceName, resourceKey, description));
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
