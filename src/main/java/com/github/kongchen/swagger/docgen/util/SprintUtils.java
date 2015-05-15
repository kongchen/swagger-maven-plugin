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