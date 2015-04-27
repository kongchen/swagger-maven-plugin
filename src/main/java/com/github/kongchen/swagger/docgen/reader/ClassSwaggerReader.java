package com.github.kongchen.swagger.docgen.reader;

import com.github.kongchen.swagger.docgen.GenerateException;
import com.wordnik.swagger.models.Swagger;

import java.util.Set;

/**
 * Created by chekong on 15/4/28.
 */
public interface ClassSwaggerReader {
    Swagger read(Set<Class<?>> classes) throws GenerateException;
}
