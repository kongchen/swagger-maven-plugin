package com.github.kongchen.swagger.docgen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.kongchen.swagger.docgen.remote.model.JAllowableListValues;
import com.github.kongchen.swagger.docgen.remote.model.JAllowableRangeValues;
import com.github.kongchen.swagger.docgen.remote.model.JAllowableValues;
import com.wordnik.swagger.model.AllowableListValues;
import com.wordnik.swagger.model.AllowableRangeValues;
import com.wordnik.swagger.model.AllowableValues;
import com.wordnik.swagger.model.AnyAllowableValues$;
import scala.Option;
import scala.Predef;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.JavaConverters;
import scala.collection.immutable.Map;
import scala.collection.mutable.Buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static <T> Option<T> getOption(T t) {
        if (t == null) return Option.empty();
        else return Option.apply(t);
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

    public static <A, B> Map<A, B> toScalaImmutableMap(HashMap<A, B> m) {
        return JavaConverters.mapAsScalaMapConverter(m).asScala().toMap(
                Predef.<Tuple2<A, B>>conforms()
        );
    }

    /**
     * uncheck*
     */
    public static <T> scala.collection.immutable.List<T> toScalaImmutableList(java.util.List<T> list) {
        if (list == null) {
            return null;
        }
        return scala.collection.immutable.List.fromIterator(JavaConversions.asScalaIterator(list.iterator()));
    }

    public static JAllowableValues getAllowableValuesFromJsonNode(JsonNode node) {
        JAllowableValues values = null;

        if(node.get("minimum") != null ) {
            String min = node.get("minimum").asText();
            String max = min;
            if (node.get("maximum") != null) {
                max = node.get("maximum").asText();                
            }
            values = new JAllowableRangeValues();
            ((JAllowableRangeValues)values).setMax(max);
            ((JAllowableRangeValues)values).setMin(min);
            
        }

        JsonNode enumNode = node.get("enum");
        if (enumNode != null && enumNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) enumNode;
            List<String> vs = new ArrayList<String>();
            for(JsonNode n : arrayNode) {
                vs.add(n.asText());
            }
            values = new JAllowableListValues();
            ((JAllowableListValues)values).setValues(vs);
            ((JAllowableListValues)values).setValueType("LIST");
            

        }
        return values;
    }

    public static String getStringFromJsonNode(JsonNode node, String key) {
        JsonNode n = node.get(key);
        if (n != null) {
            return n.asText();
        }
        else return null;
    }

    public static boolean getBooleanFromJsonNode(JsonNode node, String key) {
        
        return node.get(key) != null && node.get(key).asBoolean();
    }
}
