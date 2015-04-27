package com.github.kongchen.swagger.docgen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.CharMatcher;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 1/21/14
 */
public class SprintUtils {

    /**
     * Create a resource key from name and version
     *
     * @param resourceName
     * @param version
     * @return
     * @author tedleman
     */
    public static String createResourceKey(String resourceName, String version) {
        String resourceKey;
        if (version.length() > 0) {
            resourceKey = resourceName + "." + version;
        } else {
            resourceKey = resourceName;
        }
        resourceKey = CharMatcher.anyOf("%^#?:;").removeFrom(resourceKey);
        return resourceKey;
    }

    /**
     * Get resource name from request mapping string
     *
     * @param mapping
     * @return name of resource
     * @author tedleman
     */
    public static String parseResourceName(String mapping) {
        String resourceName = mapping;
        if (!(parseVersion(mapping).equals("")) && resourceName.contains(parseVersion(mapping))) { //get the version out if it is included
            try {
                resourceName = mapping.replaceFirst(parseVersion(mapping), "");
            } catch (PatternSyntaxException e) {
            }
        }
        while (resourceName.startsWith("/")) {
            resourceName = resourceName.substring(1);
        }
        if (resourceName.contains("/")) {
            resourceName = resourceName.substring(0, resourceName.indexOf("/"));
        }
        if (resourceName.contains("{")) {
            resourceName = "";
        }
        return resourceName;
    }


    /**
     * Get resource name from controller class
     *
     * @param clazz
     * @return
     * @author tedleman
     */
    public static String parseResourceName(Class<?> clazz) {
        String fullPath = clazz.getAnnotation(RequestMapping.class).value()[0];
        String resource = "";
        try {

            if (fullPath.endsWith("/")) {
                resource = fullPath.substring(0, fullPath.length() - 1);
            } else {
                resource = fullPath;
            }
            resource = resource.substring(resource.lastIndexOf("/"), resource.length());
            resource = resource.replaceAll("/", "");

            if (resource.equals(parseVersion(fullPath))) {
                return "";
            }
        } catch (StringIndexOutOfBoundsException e) {
            //failure in class-level mapping, method-level will be called next
        }
        return resource;
    }

    /**
     * @param mapping
     * @return version of resource
     * @author tedleman
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
}