package com.github.kongchen.swagger.docgen.remote;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.kongchen.swagger.docgen.remote.model.*;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.SwaggerSerializers;
import scala.Tuple2;

/**
 * Created by chekong on 10/13/14.
 */
public class JModelPropertyDeserializer extends com.fasterxml.jackson.databind.JsonDeserializer<JModelProperty> {

    @Override
    public JModelProperty deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JModelProperty jModelProperty = new JModelProperty();
        /**
         * private String type;
         private String qualifiedType;
         private int position;
         private boolean required;
         private String description;
         private JAllowableValues allowableValues;
         private JModelRef items;
         */
        JsonNode node = jp.readValueAsTree();//jp.getCodec().readTree(jp);
        String type = node.get("type") == null ? null : node.get("type").asText();//"type": "integer",
        jModelProperty.setType(type);
        
        String format = node.get("format")== null ? null : node.get("format").asText();//    "format": "int64",
        String realType = SwaggerSerializers.fromJsonSchemaType(new Tuple2<String, String>(type,format));
//        jModelProperty.setQualifiedType(qualifiedType);
        jModelProperty.setType(realType);
        if (format != null && format.equalsIgnoreCase("date-time")) {
            System.out.println(realType);
            jModelProperty.setType("Date");
//            jModelProperty.setQualifiedType("org.joda.time.DateTime");
        }
        
        int position = node.get("position")== null ? 0 : node.get("position").asInt();
        jModelProperty.setPosition(position);
        
        boolean required = node.get("required") == null ? false : node.get("required").asBoolean();
        jModelProperty.setRequired(required);
        
        String description = node.get("description")== null ? null : node.get("description").asText();//description": "unique identifier for the pet",
        jModelProperty.setDescription(description);
        
        String $ref = node.get("$ref")== null ? null : node.get("$ref").asText();
        jModelProperty.set$ref($ref);
        
        JsonNode jsonNode = node.get("items");
        if (jsonNode != null) {
            JModelRef items = jsonNode.traverse(jp.getCodec()).readValueAs(JModelRef.class);
            jModelProperty.setItems(items);
            if ("array".equalsIgnoreCase(type)) {
                if (Utils.getBooleanFromJsonNode(node, "uniqueItems")) {
                    jModelProperty.setType("Set");
                } else {
                    jModelProperty.setType("List");
                }
            }
        }

        JAllowableValues values = Utils.getAllowableValuesFromJsonNode(node);

        jModelProperty.setAllowableValues(values);
       
        
        return jModelProperty;
    }

}
