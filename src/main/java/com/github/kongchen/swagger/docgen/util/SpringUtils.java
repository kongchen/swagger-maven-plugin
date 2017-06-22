package com.github.kongchen.swagger.docgen.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

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
    public static String[] getControllerRequestMapping(Class<?> controllerClazz) {
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
    
	public static RequestMapping getMethodRequestMapping(Method method) {
		RequestMapping requestMapping = AnnotationUtils.findAnnotation(method,
				RequestMapping.class);
		if (requestMapping != null)
			return requestMapping;
	
		GetMapping getMapping = AnnotationUtils.findAnnotation(method,
				GetMapping.class);
		if (getMapping != null)
			return createMethodRequestMapping(method, (Class<Annotation>) getMapping.annotationType());
		
		PostMapping postMapping = AnnotationUtils.findAnnotation(method,
				PostMapping.class);
		if (postMapping != null)
			return createMethodRequestMapping(method, (Class<Annotation>) postMapping.annotationType());
		
		PutMapping putMapping = AnnotationUtils.findAnnotation(method,
				PutMapping.class);
		if (putMapping != null)
			return createMethodRequestMapping(method, (Class<Annotation>) putMapping.annotationType());
		
		PatchMapping patchMapping = AnnotationUtils.findAnnotation(method,
				PatchMapping.class);
		if (patchMapping != null)
			return createMethodRequestMapping(method, (Class<Annotation>) patchMapping.annotationType());

		DeleteMapping deleteMapping = AnnotationUtils.findAnnotation(method,
				DeleteMapping.class);
		if (patchMapping != null)
			return createMethodRequestMapping(method, (Class<Annotation>) deleteMapping.annotationType());
		
		return null;
	}

	private static RequestMapping createMethodRequestMapping(Method method,
			Class<Annotation> clazz) {
		Annotation mappingAnnotation = AnnotationUtils.findAnnotation(method,
				clazz);
		RequestMapping requestMappingAnnotation = null;
		if (mappingAnnotation != null) {
			Map<String, Object> values = AnnotationUtils
					.getAnnotationAttributes(mappingAnnotation);
			requestMappingAnnotation = AnnotationUtils.synthesizeAnnotation(
					values, RequestMapping.class, method);
		}
		return requestMappingAnnotation;
	}

}