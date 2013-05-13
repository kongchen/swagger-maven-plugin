package com.github.kongchen.swagger.docgen;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong
 * 05/13/2013
 */
public class TypeUtils {
    @JsonIgnore
    private static final List<String> basicTypes;

    static {
        String[] a = {
                "string", "boolean", "Date", "int", "Array", "long", "List", "void", "float", "double"
        };
        basicTypes = Arrays.asList(a);
    }

//    @JsonIgnore
//    private final Documentation swaggerDoc;

    @JsonIgnore
    private static final Pattern pattern = Pattern.compile("^\\w+\\[(\\w+)\\]$");

    public static String getTrueType(String dataType) {
        String t;
        Matcher m = pattern.matcher(dataType);
        if (m.find()) {
            t = m.group(1);
        } else {
            t = dataType;
        }
        if (basicTypes.contains(t)) {
            t = null;
        }

        return t;
    }

    public static String filterBasicTypes(String linkType) {
        if (basicTypes.contains(linkType)) {
            return null;
        }
        return linkType;
    }
}
