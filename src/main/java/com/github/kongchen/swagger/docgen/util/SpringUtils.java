package com.github.kongchen.swagger.docgen.util;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.google.common.base.CharMatcher;

/**
 * @author kongchen
 *         Date: 1/21/14
 * @author tedleman
 */
public class SpringUtils {

    /**
     * Create a resource key from name and version
     *
     * @param resourceName
     * @param version
     * @return
     */
    public static String createResourceKey(String resourceName, String version) {
        String resourceKey;
        if (!version.isEmpty()) {
            resourceKey = resourceName + "." + version;
        } else {
            resourceKey = resourceName;
        }
        resourceKey = CharMatcher.anyOf("%^#?:;").removeFrom(resourceKey);
        return resourceKey;
    }

    /**
     * @param mapping
     * @return version of resource
     */
    public static String parseVersion(String mapping) {
        String version = "";
        String[] mappingArray = mapping.split("/");

        for (String str : mappingArray) {
            if (str.length() < 4) {
                for (char c : str.toCharArray()) {
                    if (Character.isDigit(c)) {
                        version = str;
                        break;
                    }
                }
            }

        }

        return version;
    }

    /**
     * Extracts all routes from the annotated class
     *
     * @param controllerClazz
     *            Instrospected class
     * @return At least 1 route value (empty string)
     */
    public static String[] getControllerResquestMapping(Class<?> controllerClazz) {
	String[] controllerRequestMappingValues = {};

	// Determine if we will use class-level requestmapping or dummy string
	RequestMapping classRequestMapping = AnnotationUtils.findAnnotation(controllerClazz, RequestMapping.class);
	if (classRequestMapping != null) {
	    controllerRequestMappingValues = classRequestMapping.value();
	}

	if (controllerRequestMappingValues.length == 0) {
	    controllerRequestMappingValues = new String[1];
	    controllerRequestMappingValues[0] = "";
	}
	return controllerRequestMappingValues;
    }
}