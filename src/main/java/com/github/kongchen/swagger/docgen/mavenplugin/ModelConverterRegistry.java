package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.wordnik.swagger.converter.ModelConverters;
import com.wordnik.swagger.converter.SwaggerSchemaConverter;

import java.util.HashSet;
import java.util.Set;

public class ModelConverterRegistry {

    private static final Set<SwaggerSchemaConverter> REGISTERED_CONVERTERS = new HashSet<SwaggerSchemaConverter>();

    public static SwaggerSchemaConverter registerConverter(String className) throws GenerateException {
        try {
            SwaggerSchemaConverter schemaConverter = (SwaggerSchemaConverter) Class.forName(className).newInstance();
            ModelConverters.addConverter(schemaConverter, true);
            REGISTERED_CONVERTERS.add(schemaConverter);
            return schemaConverter;
        } catch (Exception e) {
            throw new GenerateException("Cannot load: " + className, e);
        }
    }

    public static void clear() {
        for (SwaggerSchemaConverter converter : REGISTERED_CONVERTERS) {
            ModelConverters.removeConverter(converter);
        }
        REGISTERED_CONVERTERS.clear();
    }
}
