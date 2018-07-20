package com.github.kongchen.swagger.docgen.util;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author kongchen
 *         Date: 1/21/14
 * @author tedleman
 */
public class SpringUtils {
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
