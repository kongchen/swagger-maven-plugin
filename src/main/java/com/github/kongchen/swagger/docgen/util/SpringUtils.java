package com.github.kongchen.swagger.docgen.util;

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
}