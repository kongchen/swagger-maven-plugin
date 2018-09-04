package com.github.kongchen.swagger.docgen.reader;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.SimpleType;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.converter.ModelConverterContextImpl;
import io.swagger.models.ArrayModel;
import io.swagger.models.Model;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

/**
 * Created by mkosiorek on 25.04.17.
 */
public class ModelModifierTest {

    @Test
    public void testProcessFieldInParentClass() throws Exception {
        ModelModifier modelModifier =  new ModelModifier(new ObjectMapper());
        modelModifier.setApiModelPropertyAccessExclusions(Arrays.asList("public"));

        JavaType type = SimpleType.constructUnsafe(B.class);
        ModelConverterContext context = new ModelConverterContextImpl(new ModelConverter() {
            @Override
            public Property resolveProperty(Type type, ModelConverterContext modelConverterContext, Annotation[] annotations, Iterator<ModelConverter> iterator) {
                return null;
            }

            @Override
            public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
                ArrayModel model = new ArrayModel();
                Map<String, Property> properties = new HashMap<String, Property>();
                properties.put("sample1", new StringProperty());
                properties.put("sample2", new StringProperty());
                model.setProperties(properties);
                return model;
            }
        });
        Iterator<ModelConverter> chain = null;
        Model model = modelModifier.resolve(type, context, chain);
        Assert.assertFalse(model.getProperties().containsKey("sample1"));
        Assert.assertTrue(model.getProperties().containsKey("sample2"));
    }

    static class A {

        @ApiModelProperty(name = "sample1", access = "public")
        private String sample1;

        @ApiModelProperty(name = "sample2", access = "other")
        private String sample2;
    }

    static class B extends A {

    }
}
