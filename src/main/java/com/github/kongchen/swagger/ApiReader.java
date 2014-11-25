package com.github.kongchen.swagger;

import com.wordnik.swagger.models.Swagger;

/**
 * Created by chekong on 14-11-12.
 */
public interface ApiReader {

    public Swagger read(Class clazz);
}
