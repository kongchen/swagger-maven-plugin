package com.github.kongchen.swagger.docgen.mavenplugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kongchen.swagger.docgen.GenerateException;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.BasicAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
        final Map<String, SecuritySchemeDefinition> map = new HashMap<String, SecuritySchemeDefinition>();

        List<JsonNode> securityDefinitions = new ArrayList<JsonNode>();
        if (json != null || jsonPath != null) {
            securityDefinitions = loadSecurityDefintionsFromJsonFile();
        } else {
            securityDefinitions.add(new ObjectMapper().valueToTree(this));
        }

        for (final JsonNode securityDefinition : securityDefinitions) {
            final SecuritySchemeDefinition ssd = getSecuritySchemeDefinitionByType(securityDefinition.get("type").asText(), securityDefinition);
            if (ssd != null) {
                map.put(securityDefinition.get("name").asText(), ssd);
            }
        }

        return map;
    }

    private List<JsonNode> loadSecurityDefintionsFromJsonFile() throws GenerateException {
        final List<JsonNode> securityDefinitions = new ArrayList<JsonNode>();

        try {
            final InputStream jsonStream = json != null ? this.getClass().getResourceAsStream(json) : new FileInputStream(jsonPath);
            final JsonNode tree = new ObjectMapper().readTree(jsonStream);
            final Iterator<String> securityDefinitionNameIterator = tree.fieldNames();
            while (securityDefinitionNameIterator.hasNext()) {
                final String securityDefinitionName = securityDefinitionNameIterator.next();
                JsonNode securityDefinition = tree.get(securityDefinitionName);
                securityDefinition = ((ObjectNode) securityDefinition).put("name", securityDefinitionName);
                securityDefinitions.add(securityDefinition);
            }
        } catch (final IOException e) {
            throw new GenerateException(e);
        }

        return securityDefinitions;
    }

    private SecuritySchemeDefinition getSecuritySchemeDefinitionByType(final String type, final JsonNode node) throws GenerateException {
        try {
            final ObjectMapper mapper = new ObjectMapper();
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
        } catch (final IOException e) {
            throw new GenerateException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getIn() {
        return in;
    }

    public void setIn(final String in) {
        this.in = in;
    }
}
