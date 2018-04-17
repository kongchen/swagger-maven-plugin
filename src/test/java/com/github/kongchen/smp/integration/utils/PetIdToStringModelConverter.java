package com.github.kongchen.smp.integration.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.AbstractModelConverter;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * A ModelConverter used for testing adding custom model converters.
 */
public class PetIdToStringModelConverter extends AbstractModelConverter {

    public PetIdToStringModelConverter() {
        super(new ObjectMapper());
    }

    @Override
    public Property resolveProperty(final Type type, final ModelConverterContext modelConverterContext, final Annotation[] annotations, final Iterator<ModelConverter> iterator) {
        try {
            final Type expectedType = _mapper.constructType(Class.forName("com.wordnik.sample.model.PetId"));
            if (type.equals(expectedType)) {
                return super.resolveProperty(_mapper.constructType(Class.forName("java.lang.String")), modelConverterContext, annotations, iterator);
            }
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return super.resolveProperty(type, modelConverterContext, annotations, iterator);
    }
}
