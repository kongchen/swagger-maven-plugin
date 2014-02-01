package com.github.kongchen.swagger.docgen.util;

import scala.Option;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 1/21/14
 */
public class Utils {
    public static String getStrInOption(Option<String> scalaStr ) {
        if (scalaStr.isEmpty()) return null;
        return scalaStr.get();
    }
}
