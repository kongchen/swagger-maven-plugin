package com.github.kongchen.swagger.docgen;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Swagger;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Set;

/**
 * Tests for the {@link Utils} class.
 */
public class TestUtilsClass {

    @Test
    public void testSortSwaggerOrderedProperties() throws GenerateException {
        // Create a swagger with models and properties in arbitrary order.
        Swagger swagger = new Swagger();

        HashMap<String, Property> props = new HashMap<>();
        props.put("c", new StringProperty());
        props.put("b", new IntegerProperty());
        props.put("a", new FloatProperty());

        HashMap<String, Model> definitions = new HashMap<>();
        definitions.put("c", new ModelImpl());
        definitions.put("b", new ModelImpl());
        definitions.put("a", new ModelImpl());
        definitions.forEach((name, defn) -> defn.setProperties(props));
        swagger.setDefinitions(definitions);

        // Action
        Utils.sortSwagger(swagger);

        // Verify that all names are in order
        String[] orderedNames = new String[]{"a", "b", "c"};
        Set<String> modelNames = swagger.getDefinitions().keySet();
        Assert.assertEquals(modelNames.toArray(), orderedNames);
        for (String modelName : modelNames) {
           Set<String> propNames = swagger.getDefinitions().get(modelName).getProperties().keySet();
           Assert.assertEquals(propNames.toArray(), orderedNames);
        }
    }
}
