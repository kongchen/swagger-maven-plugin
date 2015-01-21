package com.github.kongchen.swagger.docgen.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.github.kongchen.swagger.docgen.remote.model.JAllowableListValues;
import com.github.kongchen.swagger.docgen.remote.model.JAllowableRangeValues;
import com.github.kongchen.swagger.docgen.remote.model.JAllowableValues;
import com.google.common.base.CharMatcher;
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
import java.util.regex.PatternSyntaxException;

import org.springframework.web.bind.annotation.RequestMapping;

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
    
    /**
     * @author tedleman
     * @param mapping
     * @return version of resource
     */
    public static String parseVersion(String mapping){
    	String version = "";
		String[] mappingArray = mapping.split("/");
		
		for(String str: mappingArray){
			if(str.length()<4){
				for(char c: str.toCharArray()){
					if(Character.isDigit(c)){
						version=str;
						break;
					}
				}
			}
			
		}

		return version;
    }
    
    /**
     * Get resource name from request mapping string
     * @author tedleman
     * @param mapping
     * @return name of resource
     */
    public static String parseResourceName(String mapping){
    	String resourceName = mapping;
		if(!(Utils.parseVersion(mapping).equals(""))&&resourceName.contains(Utils.parseVersion(mapping))){ //get the version out if it is included
			try{
				resourceName = mapping.replaceFirst(Utils.parseVersion(mapping),"");
			}catch(PatternSyntaxException e){}
		}
		while(resourceName.startsWith("/")){
			resourceName = resourceName.substring(1);
		}
		if(resourceName.contains("/")){
			resourceName = resourceName.substring(0,resourceName.indexOf("/"));
		}
		if(resourceName.contains("{")){
			resourceName="";
		}
		return resourceName;
    }
    
    /**
     * Get resource name from controller class
     * @author tedleman
     * @param clazz
     * @return
     */
    public static String parseResourceName(Class<?> clazz){
    	String fullPath = clazz.getAnnotation(RequestMapping.class).value()[0];
		String resource="";
		try{

			if(fullPath.endsWith("/")){
				resource = fullPath.substring(0, fullPath.length()-1);
			}else{
				resource = fullPath;
			}
			resource = resource.substring(resource.lastIndexOf("/"), resource.length());
			resource = resource.replaceAll("/", "");

			if(resource.equals(Utils.parseVersion(fullPath))){
				return "";
			}
		}catch(StringIndexOutOfBoundsException e){
			//failure in class-level mapping, method-level will be called next
		}
		return resource;
    }
    
    /**
     * Create a resource key from name and version
     * @author tedleman
     * @param resourceName
     * @param version
     * @return
     */
    public static String createResourceKey(String resourceName, String version){
    	String resourceKey;
		if(version.length()>0){
			resourceKey = resourceName+"."+version;
		}else{
			resourceKey = resourceName;
		}
		resourceKey = CharMatcher.anyOf("%^#?:;").removeFrom(resourceKey);
		return resourceKey;
    }
}
