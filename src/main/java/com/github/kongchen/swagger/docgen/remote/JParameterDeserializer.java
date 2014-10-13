package com.github.kongchen.swagger.docgen.remote;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.kongchen.swagger.docgen.remote.model.JModelRef;
import com.github.kongchen.swagger.docgen.remote.model.JParameter;
import com.github.kongchen.swagger.docgen.util.Utils;
import com.wordnik.swagger.model.SwaggerSerializers;
import scala.Tuple2;

/**
 * Created by chekong on 10/13/14.
 */
public class JParameterDeserializer extends JsonDeserializer<JParameter> {
    @Override
    public JParameter deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        /**
         * private String name;
         private String description;
         private String defaultValue;
         private boolean required;
         private boolean allowMultiple;
         @JsonProperty("type")
         private String dataType;
         private JAllowableValues allowableValues;
         private String paramType;
         private String paramAccess;
         */
        JsonNode node = jp.readValueAsTree();//jp.getCodec().readTree(jp)
        JParameter parameter = new JParameter();
        parameter.setName(Utils.getStringFromJsonNode(node, "name"));
        parameter.setDescription(Utils.getStringFromJsonNode(node, "description"));
        parameter.setDefaultValue(Utils.getStringFromJsonNode(node, "defaultValue"));
        parameter.setRequired(Utils.getBooleanFromJsonNode(node, "required"));
        parameter.setAllowMultiple(Utils.getBooleanFromJsonNode(node, "allowMultiple"));
        String type = Utils.getStringFromJsonNode(node, "type");
        String format = Utils.getStringFromJsonNode(node, "format");
        parameter.setDataType(SwaggerSerializers.fromJsonSchemaType(new Tuple2<String, String>(type, format)));

        JsonNode jsonNode = node.get("items");
        if (jsonNode != null) {
            JModelRef items = jsonNode.traverse(jp.getCodec()).readValueAs(JModelRef.class);

            if ("array".equalsIgnoreCase(type)) {
                if (Utils.getBooleanFromJsonNode(node, "uniqueItems")) {
                    parameter.setDataType("Set["+items.getRef()+"]");
                } else {
                    parameter.setDataType("List["+items.getRef()+"]");
                }
            }
        }

        parameter.setAllowableValues(Utils.getAllowableValuesFromJsonNode(node));
        parameter.setParamType(Utils.getStringFromJsonNode(node, "paramType"));
        parameter.setParamAccess(Utils.getStringFromJsonNode(node, "paramAccess"));
        return parameter;
    }
}
