package com.github.kongchen.swagger.docgen.reader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.wordnik.swagger.converter.ModelConverter;
import com.wordnik.swagger.converter.ModelConverterContext;
import com.wordnik.swagger.jackson.ModelResolver;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.properties.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by chekong on 15/5/19.
 */
public class ModelSubstitute extends ModelResolver {
    private Map<Type, Type> types = new HashMap<Type, Type>();

    public ModelSubstitute(ObjectMapper mapper) {
        super(mapper);
    }

    public void substitute(String fromClass, String toClass) throws GenerateException {
        try {
            Type type = _mapper.constructType(Class.forName(fromClass));
            Type toType = _mapper.constructType(Class.forName(toClass));

            types.put(type, toType);

        } catch (ClassNotFoundException e) {
            throw new GenerateException(e);
        }
    }

    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> chain) {
        if(types.containsKey(type)) {
            return super.resolveProperty(types.get(type), context, annotations, chain);
        } else {
            return super.resolveProperty(type, context, annotations, chain);
        }

    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(types.containsKey(type)) {
            return super.resolve(types.get(type), context, chain);
        } else {
            return super.resolve(type, context, chain);
        }
    }
}
