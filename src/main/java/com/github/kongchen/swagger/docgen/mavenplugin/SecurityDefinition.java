package com.github.kongchen.swagger.docgen.mavenplugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.GenerateException;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chekong on 15/5/5.
 */
public class SecurityDefinition {
    private String name;
    private String type;
    private String description;
    private String json;

    public Map<String, SecuritySchemeDefinition> getDefinitions() throws GenerateException {
        Map<String, SecuritySchemeDefinition> map = new HashMap<String, SecuritySchemeDefinition>();
        if (name != null && type != null) {
            map.put(name, getSecuritySchemeDefinitionByNameAndType());

        } else if (json != null) {

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode tree = objectMapper.readTree(this.getClass().getResourceAsStream(json));
                Iterator<String> fit = tree.fieldNames();
                while (fit.hasNext()) {
                    String field = fit.next();
                    JsonNode node = tree.get(field);
                    String type = node.get("type").asText();
                    SecuritySchemeDefinition ssd = getSecuritySchemeDefinitionByType(type, node);
                    if (ssd != null) {
                        map.put(field, ssd);
                    }
                }
            } catch (IOException e) {
                throw new GenerateException(e);
            }

        }
        return map;
    }

    private SecuritySchemeDefinition getSecuritySchemeDefinitionByType(String type, JsonNode node) throws GenerateException {
        ObjectMapper mapper = new ObjectMapper();
        SecuritySchemeDefinition def = null;
        if (type.equals(new OAuth2Definition().getType())) {
            def = new OAuth2Definition();
            if (node != null) {
                try {
                    def = mapper.readValue(node.traverse(), OAuth2Definition.class);
                } catch (IOException e) {
                    throw new GenerateException(e);
                }
            }

        } else if (type.equals(new BasicAuthDefinition().getType())) {
            def = new BasicAuthDefinition();
            if (node != null) {
                try {
                    def = mapper.readValue(node.traverse(), BasicAuthDefinition.class);
                } catch (IOException e) {
                    throw new GenerateException(e);
                }
            }
        } else if (type.equals(new ApiKeyAuthDefinition().getType())) {
            def = new ApiKeyAuthDefinition();
            if (node != null) {
                try {
                    def = mapper.readValue(node.traverse(), ApiKeyAuthDefinition.class);
                } catch (IOException e) {
                    throw new GenerateException(e);
                }
            }
        }
        return def;
    }

    private SecuritySchemeDefinition getSecuritySchemeDefinitionByNameAndType() throws GenerateException {
        ObjectMapper mapper = new ObjectMapper();
        final String _type = type;
        final String _description = description;
        SecuritySchemeDefinition def = new SecuritySchemeDefinition() {

            private String type = _type;
            private String description = _description;
            private Map<String, Object> vendorExtensions;

            @Override
            public String getType() {
                return type;
            }

            @Override
            public void setType(String type) {
                this.type = type;
            }

            @Override
            public Map<String, Object> getVendorExtensions() {
                return vendorExtensions;
            }

            @Override
            public void setVendorExtension(String key, Object value) {
                vendorExtensions.put(key, value);
            }

            @Override
            public String getDescription() {
                return description;
            }

            @Override
            public void setDescription(String description) {
                this.description = description;
            }
        };

        JsonNode node = mapper.valueToTree(def);
        SecuritySchemeDefinition securitySchemeDefinition = getSecuritySchemeDefinitionByType(type, node);
        return securitySchemeDefinition;
    }


    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
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
}
