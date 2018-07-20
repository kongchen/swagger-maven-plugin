package com.github.kongchen.swagger.docgen.mavenplugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.GenerateException;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author chekong on 15/5/5.
 */
public class SecurityDefinition {
    private String name;
    private String type;
    private String in;
    private String description;
    private String json;
    private String jsonPath;

    public Map<String, SecuritySchemeDefinition> generateSecuritySchemeDefinitions() throws GenerateException {
        Map<String, SecuritySchemeDefinition> map = new HashMap<String, SecuritySchemeDefinition>();

        Map<String, JsonNode> securityDefinitions = new HashMap<String, JsonNode>();
        if (json != null || jsonPath != null) {
            securityDefinitions = loadSecurityDefintionsFromJsonFile();
        } else {
            JsonNode tree = new ObjectMapper().valueToTree(this);
            securityDefinitions.put(tree.get("name").asText(), tree);
        }

        for (Map.Entry<String, JsonNode> securityDefinition : securityDefinitions.entrySet()) {
            JsonNode definition = securityDefinition.getValue();
            SecuritySchemeDefinition ssd = getSecuritySchemeDefinitionByType(definition.get("type").asText(), definition);
            tryFillNameField(ssd, securityDefinition.getKey());

            if (ssd != null) {
                map.put(securityDefinition.getKey(), ssd);
            }
        }

        return map;
    }

    /**
     * <p>Try to fill the name property of some authentication definition, if no user defined value was set.</p>
     * <p>If the current value of the name property is empty, this will fill it to be the same as the name of the
     * security definition.</br>
     * If no {@link Field} named "name" is found inside the given SecuritySchemeDefinition, no action will be taken.
     *
     * @param ssd security scheme
     * @param value value to set the name to
     */
    private void tryFillNameField(SecuritySchemeDefinition ssd, String value) {
        if (ssd == null) {
            return;
        }

        Field nameField = FieldUtils.getField(ssd.getClass(), "name", true);
        try {
            if (nameField != null && nameField.get(ssd) == null) {
                nameField.set(ssd, value);
            }
        } catch (IllegalAccessException e) {
            // ignored
        }
    }

    private Map<String, JsonNode> loadSecurityDefintionsFromJsonFile() throws GenerateException {
        Map<String, JsonNode> securityDefinitions = new HashMap<String, JsonNode>();

        try {
            InputStream jsonStream = json != null ? this.getClass().getResourceAsStream(json) : new FileInputStream(jsonPath);
            JsonNode tree = new ObjectMapper().readTree(jsonStream);
            Iterator<Map.Entry<String, JsonNode>> fields = tree.fields();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode securityDefinition = field.getValue();

                securityDefinitions.put(field.getKey(), securityDefinition);
            }
        } catch (IOException e) {
            throw new GenerateException(e);
        }

        return securityDefinitions;
    }

    private SecuritySchemeDefinition getSecuritySchemeDefinitionByType(String type, JsonNode node) throws GenerateException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            SecuritySchemeDefinition def = null;
            if (type.equals(new OAuth2Definition().getType())) {
                def = new OAuth2Definition();
                if (node != null) {
                    def = mapper.readValue(node.traverse(), OAuth2Definition.class);
                }
            } else if (type.equals(new BasicAuthDefinition().getType())) {
                def = new BasicAuthDefinition();
                if (node != null) {
                    def = mapper.readValue(node.traverse(), BasicAuthDefinition.class);
                }
            } else if (type.equals(new ApiKeyAuthDefinition().getType())) {
                def = new ApiKeyAuthDefinition();
                if (node != null) {
                    def = mapper.readValue(node.traverse(), ApiKeyAuthDefinition.class);
                }
            }
            return def;
        } catch (IOException e) {
            throw new GenerateException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIn() {
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
