package com.github.kongchen.smp.integration.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.AbstractModelConverter;
import io.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

public class PetIdToStringModelConverter extends AbstractModelConverter {

    public PetIdToStringModelConverter() {
        super(new ObjectMapper());
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext modelConverterContext, Annotation[] annotations, Iterator<ModelConverter> iterator) {
        try {
            Type expectedType = _mapper.constructType(Class.forName("com.wordnik.sample.model.PetId"));
            if (type.equals(expectedType)) {
                return super.resolveProperty(_mapper.constructType(Class.forName("java.lang.String")), modelConverterContext, annotations, iterator);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return iterator.hasNext() ? iterator.next().resolveProperty(type, modelConverterContext, annotations, iterator) :
                super.resolveProperty(type, modelConverterContext, annotations, iterator);
    }
}
