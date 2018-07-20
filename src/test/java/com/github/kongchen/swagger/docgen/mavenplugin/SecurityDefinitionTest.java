package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.GenerateException;
import io.swagger.models.auth.ApiKeyAuthDefinition;
import io.swagger.models.auth.OAuth2Definition;
import io.swagger.models.auth.SecuritySchemeDefinition;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class SecurityDefinitionTest {
    @Test
    public void testSecurityDefinitionRetainsWantedName() throws GenerateException {
        SecurityDefinition definition = new SecurityDefinition();
        definition.setJson("securityDefinition.json");

        Map<String, SecuritySchemeDefinition> definitions = definition.generateSecuritySchemeDefinitions();

        SecuritySchemeDefinition api_key = definitions.get("api_key");
        Assert.assertNotNull(api_key);
        Assert.assertTrue(api_key instanceof ApiKeyAuthDefinition);
        Assert.assertEquals(((ApiKeyAuthDefinition)api_key).getName(), "api_key_name");

        // No name is set for this auth
        // The name should be set to the name of the definition
        // So that the name is never actually empty
        SecuritySchemeDefinition api_key_empty_name = definitions.get("api_key_empty_name");
        Assert.assertNotNull(api_key_empty_name);
        Assert.assertTrue(api_key_empty_name instanceof ApiKeyAuthDefinition);
        Assert.assertEquals(((ApiKeyAuthDefinition)api_key_empty_name).getName(), "api_key_empty_name");


        SecuritySchemeDefinition petstore_auth = definitions.get("petstore_auth");
        Assert.assertNotNull(petstore_auth);
        Assert.assertTrue(petstore_auth instanceof OAuth2Definition);
    }
}
