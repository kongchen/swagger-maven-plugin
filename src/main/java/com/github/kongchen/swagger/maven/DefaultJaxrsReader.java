package com.github.kongchen.swagger.maven;

import com.github.kongchen.smp.ApiReader;
import com.wordnik.swagger.jaxrs.Reader;
import com.wordnik.swagger.models.Swagger;


/**
 * Created by chekong on 14-11-12.
 */
public class DefaultJaxrsReader implements ApiReader {
    Reader reader;
    public DefaultJaxrsReader() {
        this.reader = new Reader(new Swagger());
    }

    @Override
    public Swagger read(Class clazz) {
        return reader.read(clazz);
    }
}
