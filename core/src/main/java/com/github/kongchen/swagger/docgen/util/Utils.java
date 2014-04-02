package com.github.kongchen.swagger.docgen.util;

import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableRangeValues;
import com.wordnik.swagger.model.AllowableValues;
import com.wordnik.swagger.model.AnyAllowableValues$;
import scala.Option;
import scala.collection.JavaConversions;
import scala.collection.mutable.Buffer;

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

    public static String allowableValuesToString(AllowableValues allowableValues) {
        if (allowableValues == null) {
            return null;
        }
        String values = "";
        if (allowableValues instanceof AllowableListValues) {
            Buffer<String> buffer = ((AllowableListValues) allowableValues).values().toBuffer();
            for (String aVlist : JavaConversions.asJavaList(buffer)) {
                values += aVlist.trim() + ", ";
            }
            values = values.trim();
            values = values.substring(0, values.length() - 1);
        } else if (allowableValues instanceof AllowableRangeValues) {
            String max = ((AllowableRangeValues) allowableValues).max();
            String min = ((AllowableRangeValues) allowableValues).min();
            values = min + " to " + max;

        } else if (allowableValues instanceof AnyAllowableValues$) {
            return values;
        }
        return values;
    }
}
